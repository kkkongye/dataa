package cn.hdu.liu.mapper;

import cn.hdu.liu.obj.DataObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GovernanceMapper {
    @Select("SELECT * FROM data_objects WHERE id = #{id}")
    DataObject search(String id);
}
