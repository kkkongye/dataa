package cn.hdu.liu.mapper;

import cn.hdu.liu.obj.TokenRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TokenRequestMapper {
    @Insert("INSERT INTO token_requests (applicant_id, applicant_role, data_object_ids, requested_at) " +
            "VALUES (#{applicantId}, #{applicantRole}, #{dataObjectIds}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TokenRequest tokenRequest);

    @Update("UPDATE token_requests SET " +
            "status = #{status}, " +
            "token = #{token}, " +
            "reviewed_at = NOW(), " +
            "reviewer_id = #{reviewerId} " +
            "WHERE id = #{id}")
    void update(TokenRequest tokenRequest);

    @Select("SELECT * FROM token_requests WHERE status = 'pending'")
    List<TokenRequest> selectPendingRequests();

    @Select("SELECT * FROM token_requests WHERE applicant_id = #{applicantId}")
    List<TokenRequest> selectByApplicantId(Integer applicantId);

    @Select("SELECT * FROM token_requests WHERE reviewer_id = #{reviewerId}")
    List<TokenRequest> selectByReviewerId(Integer reviewerId);

    @Select("SELECT * FROM token_requests " +
            "WHERE applicant_id = #{applicantId} AND status = 'approved' " +
            "ORDER BY requested_at DESC " +
            "LIMIT 1")
    TokenRequest findLatestApprovedByApplicantId(Integer applicantId);

    @Select("SELECT * FROM token_requests WHERE id = #{id}")
    TokenRequest selectById(Integer id);

    @Select("SELECT * FROM token_requests WHERE token = #{token}")
    TokenRequest findByToken(String token);
}