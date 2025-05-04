package cn.hdu.liu.obj;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 数字对象实体类
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataObject {
    /** 唯一标识符 */
    private String id;

    /** 数据库自增长ID */
    private Long numericId;


    /** 约束条件集合 */
    private ConstraintSet constraintSet;

    /** 传播控制操作集合 */
    private PropagationControl propagationControl =new PropagationControl();

    @JsonProperty("propagationControl")
    public PropagationControl getPropagationControl() {
        return this.propagationControl;
    }

    /** 审计控制信息集合 */
    private AuditInfo auditInfo;

    /** 名称 */
    private String name;

    /** 创建者 */
    private String creator;

    /** 创建时间 */
    private Timestamp createdAt;

    /** 更新时间 */
    private Timestamp updatedAt;


    private DataEntity dataEntity;

    private LocationInfo locationInfo;

    // 新增字段用于存储JSON字符串
    private String dataContent;
    private String metadataJson;
    private String locationInfoJson;
    private String constraintSetJson;
    private String propagationControlJson;
    private String auditInfoJson;

    public DataObject() {
        this.id = UUID.randomUUID().toString();
        this.propagationControl = new PropagationControl();
        // 设置默认权限
        //  this.propagationControl.setCanModify(true);
        //  this.propagationControl.setCanRead(true);
        // this.propagationControl.setCanShare(true);
        //  this.propagationControl.setCanDelegate(true);
        //  this.propagationControl.setCanDestroy(true);
    }


    // 手动添加JSON字段的setter和getter
    public String getDataContent() {
        return dataContent;
    }

    public void setDataContent(String dataContent) {
        this.dataContent = dataContent;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public String getLocationInfoJson() {
        return locationInfoJson;
    }

    public void setLocationInfoJson(String locationInfoJson) {
        this.locationInfoJson = locationInfoJson;
    }

    public String getConstraintSetJson() {
        return constraintSetJson;
    }

    public void setConstraintSetJson(String constraintSetJson) {
        this.constraintSetJson = constraintSetJson;
    }

    public String getPropagationControlJson() {
        return propagationControlJson;
    }

    public void setPropagationControlJson(String propagationControlJson) {
        this.propagationControlJson = propagationControlJson;
    }

    public String getAuditInfoJson() {
        return auditInfoJson;
    }

    public void setAuditInfoJson(String auditInfoJson) {
        this.auditInfoJson = auditInfoJson;
    }






    public DataEntity getDataEntity() {
        return dataEntity;
    }

    /**
     * 设置数据实体
     * @param dataEntity 数据实体
     */
    public void setDataEntity(DataEntity dataEntity) {
        this.dataEntity = dataEntity;
    }

    @JsonProperty("locationInfo")
    public LocationInfo getLocationInfo() {
        return locationInfo;
    }

    /**
     * 设置数据定位信息
     * @param locationInfo 数据定位信息
     */
    public void setLocationInfo(LocationInfo locationInfo) {
        this.locationInfo = locationInfo;
    }



    /**
     * 设置ID
     * @param id 唯一标识符
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取ID
     * @return 唯一标识符
     */
    public String getId() {
        return this.id;
    }

    /**
     * 设置数据库自增长ID
     * @param numericId 数据库自增长ID
     */
    public void setNumericId(Long numericId) {
        this.numericId = numericId;
    }

    /**
     * 获取数据库自增长ID
     * @return 数据库自增长ID
     */
    public Long getNumericId() {
        return this.numericId;
    }



    /**
     * 获取数据实体
     * @return 数据实体
     */



    /**
     * 设置约束条件集合
     * @param constraintSet 约束条件集合
*/
    public void setConstraintSet(ConstraintSet constraintSet) {
        this.constraintSet = constraintSet;
    }

    /**
     * 获取约束条件集合
     * @return 约束条件集合
*/
    public ConstraintSet getConstraintSet() {
        return this.constraintSet;
    }

    /**
     * 设置传播控制操作集合
     * @param propagationControl 传播控制操作集合
     */
    public void setPropagationControl(PropagationControl propagationControl) {
        this.propagationControl = propagationControl;
    }



    /**
     * 设置审计控制信息集合
     * @param auditInfo 审计控制信息集合
     */
    public void setAuditInfo(AuditInfo auditInfo) {
        this.auditInfo = auditInfo;
    }

    /**
     * 获取审计控制信息集合
     * @return 审计控制信息集合
     */
    public AuditInfo getAuditInfo() {
        return this.auditInfo;
    }

    /**
     * 设置名称
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取名称
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置创建者
     * @param creator 创建者
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * 获取创建者
     * @return 创建者
     */
    public String getCreator() {
        return this.creator;
    }

    /**
     * 设置创建时间
     * @param createdAt 创建时间
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取创建时间
     * @return 创建时间
     */
    public Timestamp getCreatedAt() {
        return this.createdAt;
    }

    /**
     * 设置更新时间
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 获取更新时间
     * @return 更新时间
     */
    public Timestamp getUpdatedAt() {
        return this.updatedAt;
    }
}