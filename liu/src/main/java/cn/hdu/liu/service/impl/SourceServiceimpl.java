package cn.hdu.liu.service.impl;

import cn.hdu.liu.mapper.SourceMapper;
import cn.hdu.liu.obj.DataObject;
import cn.hdu.liu.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SourceServiceimpl implements SourceService {
    @Autowired
    private SourceMapper sourceMapper;

    @Override
    public void delete(Integer id) {
        sourceMapper.delete(id);
    }


    @Override
    public DataObject search(Integer id) {
        return sourceMapper.search(id);
    }


    @Override
    public void add(DataObject dataObject) {
        dataObject.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        dataObject.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        sourceMapper.insert(dataObject);
    }

    @Override
    public void update(Integer id, DataObject dataObject) {
        // 校验数据是否存在
        DataObject existing = sourceMapper.search(id);
        if (existing == null) {
            throw new RuntimeException("ID为 " + id + " 的数据不存在");
        }

        // 设置更新时间并更新
        dataObject.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        sourceMapper.update(dataObject);
    }

    @Override
    public List<DataObject> list(){
        return sourceMapper.list();
    }
}
