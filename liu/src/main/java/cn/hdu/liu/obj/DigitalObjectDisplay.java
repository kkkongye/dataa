package cn.hdu.liu.obj;

public class DigitalObjectDisplay {
    private String objectId;
    private String entity;
    private String constraintControl;
    private String status;
    private Boolean sourceAgreed;
    private Boolean governanceAgreed;

    // Getter 和 Setter 方法

    // objectId 属性
    public String getObjectId() {
        return objectId;
    }
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    // entity 属性
    public String getEntity() {
        return entity;
    }
    public void setEntity(String entity) {
        this.entity = entity;
    }

    // constraintControl 属性
    public String getConstraintControl() {
        return constraintControl;
    }
    public void setConstraintControl(String constraintControl) {
        this.constraintControl = constraintControl;
    }

    // status 属性
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    // sourceAgreed 属性（Boolean 类型）
    public Boolean getSourceAgreed() {
        return sourceAgreed;
    }
    public void setSourceAgreed(Boolean sourceAgreed) {
        this.sourceAgreed = sourceAgreed;
    }

    // governanceAgreed 属性（Boolean 类型）
    public Boolean getGovernanceAgreed() {
        return governanceAgreed;
    }
    public void setGovernanceAgreed(Boolean governanceAgreed) {
        this.governanceAgreed = governanceAgreed;
    }
}