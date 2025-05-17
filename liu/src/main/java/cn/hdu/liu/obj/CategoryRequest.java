package cn.hdu.liu.obj;

public class CategoryRequest {
    private String industryCategory;
    private String processingTimeCategory;
    private String dataSourceCategory;


    public String getIndustryCategory() {
        return industryCategory;
    }

    public void setIndustryCategory(String industryCategory) {
        this.industryCategory = industryCategory;
    }


    public String getProcessingTimeCategory() {
        return processingTimeCategory;
    }

    public void setProcessingTimeCategory(String processingTimeCategory) {
        this.processingTimeCategory = processingTimeCategory;
    }


    public String getDataSourceCategory() {
        return dataSourceCategory;
    }

    public void setDataSourceCategory(String dataSourceCategory) {
        this.dataSourceCategory = dataSourceCategory;
    }
}