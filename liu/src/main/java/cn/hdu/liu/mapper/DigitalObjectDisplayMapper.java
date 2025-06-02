package cn.hdu.liu.mapper;

import cn.hdu.liu.obj.DigitalObjectDisplay;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DigitalObjectDisplayMapper {

    @Select("SELECT * FROM digital_object_display")
    List<DigitalObjectDisplay> selectAllDisplayObjects();

    @Select("SELECT * FROM digital_object_display WHERE object_id = #{objectId}")
    DigitalObjectDisplay selectByObjectId(String objectId);

    @Update("UPDATE digital_object_display SET " +
            "source_agreed = #{sourceAgreed}, " +
            "governance_agreed = #{governanceAgreed} " +
            "WHERE object_id = #{objectId}")
    void updateAgreementStatus(@Param("objectId") String objectId,
                               @Param("sourceAgreed") Boolean sourceAgreed,
                               @Param("governanceAgreed") Boolean governanceAgreed);

    @Insert("INSERT INTO digital_object_display (object_id, entity, constraint_control, status) " +
            "VALUES (#{objectId}, #{entity}, #{constraintControl}, #{status}) " +
            "ON DUPLICATE KEY UPDATE " +
            "entity = VALUES(entity), " +
            "constraint_control = VALUES(constraint_control), " +
            "status = VALUES(status)")
    void insertOrUpdate(DigitalObjectDisplay display);
}