package cn.hdu.liu.obj;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class Metadata {
    private String dataName;
    private String sourceUnit;
    private String contactPerson;
    private String contactPhone;
    private String resourceSummary;
    private String fieldClassification;
    private List<String> headers = new ArrayList<>();

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    // Getter and Setter for sourceUnit
    public String getSourceUnit() {
        return sourceUnit;
    }

    public void setSourceUnit(String sourceUnit) {
        this.sourceUnit = sourceUnit;
    }

    // Getter and Setter for contactPerson
    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    // Getter and Setter for contactPhone
    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    // Getter and Setter for resourceSummary
    public String getResourceSummary() {
        return resourceSummary;
    }

    public void setResourceSummary(String resourceSummary) {
        this.resourceSummary = resourceSummary;
    }

    // Getter and Setter for fieldClassification
    public String getFieldClassification() {
        return fieldClassification;
    }

    public void setFieldClassification(String fieldClassification) {
        this.fieldClassification = fieldClassification;
    }

    // Getter and Setter for headers
    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers != null ? new ArrayList<>(headers) : new ArrayList<>();
    }
}