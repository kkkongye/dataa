package cn.hdu.liu.mapper;

import cn.hdu.liu.obj.DataObject;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import java.util.List;

@Mapper
public interface DataMapper {
    @Insert({
            "INSERT INTO data_objects (id, data_content, metadata_json, location_info_json, ",
            "constraint_set_json, propagation_control_json, audit_info_json, created_at, updated_at,",
            "db_grade, table_grade, row_grades, column_grades)",
            "VALUES (",
            "#{id}, ",
            "#{dataContent}, ",
            "#{metadataJson}, ",
            "#{locationInfoJson}, ",
            "#{constraintSetJson}, ",
            "#{propagationControlJson}, ",
            "#{auditInfoJson}, ",
            "NOW(), NOW(),",
            "#{dbGrade}, #{tableGrade}, ",
            "#{rowGradesJson}, ",
            "#{columnGradesJson})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "numericId")
    void insert(DataObject dataObject);

    @Select("SELECT " +
            "id, numeric_id, " +
            "CONVERT(data_content USING utf8) as data_content, " +
            "CONVERT(metadata_json USING utf8) as metadata_json, " +
            "CONVERT(location_info_json USING utf8) as location_info_json, " +
            "CONVERT(constraint_set_json USING utf8) as constraint_set_json, " +
            "CONVERT(propagation_control_json USING utf8) as propagation_control_json, " +
            "CONVERT(audit_info_json USING utf8) as audit_info_json, " +
            "db_grade, table_grade, row_grades, column_grades, " +
            "created_at, updated_at " +
            "FROM data_objects WHERE id = #{id}")
    @Results({
            @Result(column = "data_content", property = "dataContent", jdbcType = JdbcType.VARCHAR),
            @Result(column = "metadata_json", property = "metadataJson", jdbcType = JdbcType.VARCHAR),
            @Result(column = "location_info_json", property = "locationInfoJson", jdbcType = JdbcType.VARCHAR),
            @Result(column = "constraint_set_json", property = "constraintSetJson", jdbcType = JdbcType.VARCHAR),
            @Result(column = "propagation_control_json", property = "propagationControlJson", jdbcType = JdbcType.VARCHAR),
            @Result(column = "audit_info_json", property = "auditInfoJson", jdbcType = JdbcType.VARCHAR),
            @Result(column = "db_grade", property = "dbGrade"),
            @Result(column = "table_grade", property = "tableGrade"),
            @Result(column = "row_grades", property = "rowGrades",jdbcType = JdbcType.VARCHAR),
            @Result(column = "column_grades", property = "columnGrades",jdbcType = JdbcType.VARCHAR)
    })
    DataObject selectById(String id);

    @Update({
            "UPDATE data_objects SET",
            "data_content = #{dataContent,jdbcType=VARCHAR},",
            "metadata_json = #{metadataJson,jdbcType=VARCHAR},",
            "location_info_json = #{locationInfoJson,jdbcType=VARCHAR},",
            "constraint_set_json = #{constraintSetJson,jdbcType=VARCHAR},",
            "propagation_control_json = #{propagationControlJson,jdbcType=VARCHAR},",
            "audit_info_json = #{auditInfoJson,jdbcType=VARCHAR},",
            "db_grade = #{dbGrade},",
            "table_grade = #{tableGrade},",
            "row_grades = #{rowGradesJson},",
            "column_grades = #{columnGradesJson},",
            "updated_at = NOW() ",
            "WHERE id = #{id}"
    })
    void update(DataObject dataObject);

    @Delete("DELETE FROM data_objects WHERE id = #{id}")
    void delete(String id);

    @Select("SELECT " +
            "id, numeric_id, " +
            "CONVERT(data_content USING utf8) as data_content, " +
            "CONVERT(metadata_json USING utf8) as metadata_json, " +
            "CONVERT(location_info_json USING utf8) as location_info_json, " +
            "CONVERT(constraint_set_json USING utf8) as constraint_set_json, " +
            "CONVERT(propagation_control_json USING utf8) as propagation_control_json, " +
            "CONVERT(audit_info_json USING utf8) as audit_info_json, " +
            "db_grade, table_grade, row_grades, column_grades, " +
            "created_at, updated_at " +
            "FROM data_objects")
    @Results({
            @Result(column = "data_content", property = "dataContent", jdbcType = JdbcType.VARCHAR),
            @Result(column = "metadata_json", property = "metadataJson", jdbcType = JdbcType.VARCHAR),
            @Result(column = "location_info_json", property = "locationInfoJson", jdbcType = JdbcType.VARCHAR),
            @Result(column = "constraint_set_json", property = "constraintSetJson", jdbcType = JdbcType.VARCHAR),
            @Result(column = "propagation_control_json", property = "propagationControlJson", jdbcType = JdbcType.VARCHAR),
            @Result(column = "audit_info_json", property = "auditInfoJson", jdbcType = JdbcType.VARCHAR),
            @Result(column = "db_grade", property = "dbGrade"),
            @Result(column = "table_grade", property = "tableGrade"),
            @Result(column = "row_grades", property = "rowGrades",jdbcType = JdbcType.VARCHAR),
            @Result(column = "column_grades", property = "columnGrades",jdbcType = JdbcType.VARCHAR)
    })
    List<DataObject> selectAll();
}