package cn.hdu.liu.mapper;

import cn.hdu.liu.obj.DataObject;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SourceMapper {
    @Delete("delete from data_objects where id = #{id}")
    void delete(Integer id);

    @Insert({
            "INSERT INTO data_objects (id, data_content, metadata_json, location_info_json,",
            "constraint_set_json, propagation_control_json, audit_info_json, created_at, updated_at)",
            "VALUES (#{id}, #{dataContent}, #{metadataJson}, #{locationInfoJson},",
            "#{constraintSetJson}, #{propagationControlJson}, #{auditInfoJson}, NOW(), NOW())"
    })
    @Options(useGeneratedKeys = true, keyProperty = "numericId")
    void insert(DataObject dataObject);

    @Select("select * from data_objects where id = #{id}")
    DataObject search(Integer id);

    @Update({
            "UPDATE data_objects SET",
            "data_content = #{dataContent},",
            "metadata_json = #{metadataJson},",
            "location_info_json = #{locationInfoJson},",
            "constraint_set_json = #{constraintSetJson},",
            "propagation_control_json = #{propagationControlJson},",
            "audit_info_json = #{auditInfoJson},",
            "updated_at = NOW()",
            "WHERE id = #{id}"
    })
    void update(DataObject dataObject);

    @Select("select * from data_objects")
    List<DataObject> list();
}