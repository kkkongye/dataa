package cn.hdu.liu.obj;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;


public class LocationInfo {
    private String databaseName;    // 数据库名
    private String tableName;       // 表名
    private String selectFields;    // 选择的字段列表

    // 手动添加的 getter 和 setter
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSelectFields() {
        return selectFields;
    }

    public void setSelectFields(String selectFields) {
        this.selectFields = selectFields;
    }
}