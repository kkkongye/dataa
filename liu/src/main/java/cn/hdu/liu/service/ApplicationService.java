package cn.hdu.liu.service;

import cn.hdu.liu.mapper.ApplicationRecordMapper;
import cn.hdu.liu.mapper.DigitalObjectDisplayMapper;
import cn.hdu.liu.obj.ApplicationRecord;
import cn.hdu.liu.obj.DigitalObjectDisplay;
import cn.hdu.liu.obj.DataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRecordMapper applicationRecordMapper;

    @Autowired
    private DigitalObjectDisplayMapper displayMapper;

    @Autowired
    private DataObjectService dataObjectService;

    // 同步所有数据对象到展示表
    @Transactional
    public void syncAllDataObjects() {
        List<DataObject> dataObjects = dataObjectService.findAll();
        for (DataObject dataObject : dataObjects) {
            syncDataObjectToDisplay(dataObject);
        }
    }

    // 同步单个数据对象到展示表
    @Transactional
    public void syncDataObjectToDisplay(DataObject dataObject) {
        DigitalObjectDisplay display = new DigitalObjectDisplay();
        display.setObjectId(dataObject.getId());

        if (dataObject.getDataEntity() != null) {
            display.setEntity(dataObject.getDataEntity().getEntity());
            display.setStatus(dataObject.getDataEntity().getStatus());
        }

        if (dataObject.getConstraintSet() != null) {
            // 简化为字符串表示，实际可根据业务需求调整
            display.setConstraintControl(dataObject.getConstraintSet().toString());
        }

        // 保留原有的同意状态（如果存在）
        DigitalObjectDisplay existing = displayMapper.selectByObjectId(dataObject.getId());
        if (existing != null) {
            display.setSourceAgreed(existing.getSourceAgreed());
            display.setGovernanceAgreed(existing.getGovernanceAgreed());
        } else {
            display.setSourceAgreed(false);
            display.setGovernanceAgreed(false);
        }

        displayMapper.insertOrUpdate(display);
    }

    // 用户申请数字对象
    @Transactional
    public void applyForObject(String objectId, String applicant) {
        // 获取数字对象信息
        DigitalObjectDisplay display = displayMapper.selectByObjectId(objectId);
        if (display == null) {
            throw new RuntimeException("数字对象不存在: " + objectId);
        }

        // 创建申请记录
        ApplicationRecord record = new ApplicationRecord();
        record.setObjectId(objectId);
        record.setApplicant(applicant);
        record.setEntity(display.getEntity());

        // 重置同意状态
        record.setSourceAgreed(false);
        record.setGovernanceAgreed(false);

        applicationRecordMapper.insert(record);

        // 更新展示表状态
        displayMapper.updateAgreementStatus(objectId, false, false);
    }

    public List<DigitalObjectDisplay> getAllDisplayObjects() {
        return displayMapper.selectAllDisplayObjects();
    }

    public List<ApplicationRecord> getAllApplications() {
        return applicationRecordMapper.selectAllRecords();
    }

    @Transactional
    public void approveBySource(Long recordId) {
        ApplicationRecord record = applicationRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("申请记录不存在: " + recordId);
        }

        applicationRecordMapper.updateSourceAgreement(recordId, true);

        // 检查是否全部同意
        ApplicationRecord latest = applicationRecordMapper.selectLatestByObjectId(record.getObjectId());
        boolean bothAgreed = Boolean.TRUE.equals(latest.getSourceAgreed()) &&
                Boolean.TRUE.equals(latest.getGovernanceAgreed());

        // 更新展示表状态
        displayMapper.updateAgreementStatus(
                record.getObjectId(),
                true,
                bothAgreed
        );
    }

    @Transactional
    public void approveByGovernance(Long recordId) {
        ApplicationRecord record = applicationRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("申请记录不存在: " + recordId);
        }

        applicationRecordMapper.updateGovernanceAgreement(recordId, true);

        // 检查是否全部同意
        ApplicationRecord latest = applicationRecordMapper.selectLatestByObjectId(record.getObjectId());
        boolean bothAgreed = Boolean.TRUE.equals(latest.getSourceAgreed()) &&
                Boolean.TRUE.equals(latest.getGovernanceAgreed());

        // 更新展示表状态
        displayMapper.updateAgreementStatus(
                record.getObjectId(),
                bothAgreed,
                true
        );
    }
}