package cn.hdu.liu.obj;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.*;


/**
 * 数据实体类 - 表示数据实体集合I=(M,i1,i2,….,ik,…,in)
 */
@Data
public class DataEntity {
    private String entity;
    private String status;
    private static final Set<String> ALLOWED_STATUSES = Set.of("待检验", "已合格", "不合格");
    private String feedback;
    private Metadata metadata;
    private List<Map<String, String>> dataItems = new ArrayList<>();
    private Map<Integer, Map<String, Object>> data = new LinkedHashMap<>();

    public DataEntity() {}

    public DataEntity(String entity, String status, String feedback, Metadata metadata,
                      List<Map<String, String>> dataItems, Map<Integer, Map<String, Object>> data) {
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("无效状态值，允许的状态为：" + ALLOWED_STATUSES);
        }
        this.entity = entity;
        this.status = status;
        this.feedback = feedback;
        this.metadata = metadata;
        this.dataItems = dataItems != null ? new ArrayList<>(dataItems) : new ArrayList<>();
        this.data = data != null ? new LinkedHashMap<>(data) : new LinkedHashMap<>();
    }


    public void setStatus(String status) {
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("无效状态值，允许的状态为：" + ALLOWED_STATUSES);
        }
        this.status = status;
    }


    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    // Getter and Setter for status
    public String getStatus() {
        return status;
    }



    // Getter and Setter for feedback
    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    // Getter and Setter for metadata
    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    // Getter and Setter for dataItems
    public List<Map<String, String>> getDataItems() {
        return dataItems;
    }

    public void setDataItems(List<Map<String, String>> dataItems) {
        this.dataItems = dataItems != null ? new ArrayList<>(dataItems) : new ArrayList<>();
    }

    // Getter and Setter for data
    public Map<Integer, Map<String, Object>> getData() {
        return data;
    }

    public void setData(Map<Integer, Map<String, Object>> data) {
        this.data = data != null ? new LinkedHashMap<>(data) : new LinkedHashMap<>();
    }

    // 核心方法保留
    public void addData(int rowNumber, Map<String, Object> rowData) {
        data.put(rowNumber, rowData);
    }

    @JsonIgnore
    public int getRowCount() {
        return data.size();
    }

    public void addDataItem(Map<String, String> dataItem) {
        this.dataItems.add(dataItem);
    }

    @JsonIgnore
    public int getDataItemCount() {
        return dataItems.size();
    }
}