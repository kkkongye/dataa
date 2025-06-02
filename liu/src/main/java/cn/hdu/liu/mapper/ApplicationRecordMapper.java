package cn.hdu.liu.mapper;

import cn.hdu.liu.obj.ApplicationRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ApplicationRecordMapper {

    @Insert("INSERT INTO application_record (object_id, applicant, entity) " +
            "VALUES (#{objectId}, #{applicant}, #{entity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ApplicationRecord record);

    @Select("SELECT * FROM application_record")
    List<ApplicationRecord> selectAllRecords();

    @Select("SELECT * FROM application_record WHERE id = #{id}")
    ApplicationRecord selectById(Long id);

    @Update("UPDATE application_record SET source_agreed = #{sourceAgreed} WHERE id = #{id}")
    void updateSourceAgreement(@Param("id") Long id, @Param("sourceAgreed") Boolean sourceAgreed);

    @Update("UPDATE application_record SET governance_agreed = #{governanceAgreed} WHERE id = #{id}")
    void updateGovernanceAgreement(@Param("id") Long id, @Param("governanceAgreed") Boolean governanceAgreed);

    @Select("SELECT * FROM application_record WHERE object_id = #{objectId} ORDER BY apply_time DESC LIMIT 1")
    ApplicationRecord selectLatestByObjectId(String objectId);
}