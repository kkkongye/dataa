package cn.hdu.liu.obj;

import java.sql.Timestamp;

public class TokenRequest {
    private Integer id;
    private Integer applicantId;
    private String applicantRole;
    private String dataObjectIds;
    private String token;
    private String status;
    private Timestamp requestedAt;
    private Timestamp reviewedAt;
    private Integer reviewerId;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getApplicantId() { return applicantId; }
    public void setApplicantId(Integer applicantId) { this.applicantId = applicantId; }

    public String getApplicantRole() { return applicantRole; }
    public void setApplicantRole(String applicantRole) { this.applicantRole = applicantRole; }

    public String getDataObjectIds() { return dataObjectIds; }
    public void setDataObjectIds(String dataObjectIds) { this.dataObjectIds = dataObjectIds; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Timestamp requestedAt) { this.requestedAt = requestedAt; }

    public Timestamp getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Timestamp reviewedAt) { this.reviewedAt = reviewedAt; }

    public Integer getReviewerId() { return reviewerId; }
    public void setReviewerId(Integer reviewerId) { this.reviewerId = reviewerId; }
}