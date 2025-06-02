package cn.hdu.liu.obj;

import java.sql.Timestamp;

public class ApplicationRecord {
    private Long id;
    private String objectId;
    private String applicant;
    private String entity;
    private Boolean sourceAgreed;
    private Boolean governanceAgreed;
    private Timestamp applyTime;

    // Getter 和 Setter 方法

    // ID 属性
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    // ObjectId 属性
    public String getObjectId() {
        return objectId;
    }
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    // Applicant 属性
    public String getApplicant() {
        return applicant;
    }
    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    // Entity 属性
    public String getEntity() {
        return entity;
    }
    public void setEntity(String entity) {
        this.entity = entity;
    }

    // SourceAgreed 属性（Boolean 类型）
    public Boolean getSourceAgreed() {
        return sourceAgreed;
    }
    public void setSourceAgreed(Boolean sourceAgreed) {
        this.sourceAgreed = sourceAgreed;
    }

    // GovernanceAgreed 属性（Boolean 类型）
    public Boolean getGovernanceAgreed() {
        return governanceAgreed;
    }
    public void setGovernanceAgreed(Boolean governanceAgreed) {
        this.governanceAgreed = governanceAgreed;
    }

    // ApplyTime 属性（Timestamp 类型）
    public Timestamp getApplyTime() {
        return applyTime;
    }
    public void setApplyTime(Timestamp applyTime) {
        this.applyTime = applyTime;
    }
}