package cn.hdu.liu.obj;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class LocationInfo {
    private List<TableLocation> locations = new ArrayList<>();  // 存储表格位置信息


    public List<TableLocation> getLocations() {
        return locations;
    }

    // 新增setter
    public void setLocations(List<TableLocation> locations) {
        this.locations = locations;
    }
    @Data
    public static class TableLocation {
        private String sheet;       // 工作表名
        private String startRow;    // 起始行
        private String endRow;      // 结束行
        private String startColumn; // 起始列
        private String endColumn;   // 结束列

        // 添加无参构造函数，用于Jackson反序列化
        public TableLocation() {
        }

        public String getSheet() {
            return sheet;
        }

        public void setSheet(String sheet) {
            this.sheet = sheet;
        }

        public String getStartRow() {
            return startRow;
        }

        public void setStartRow(String startRow) {
            this.startRow = startRow;
        }

        public String getEndRow() {
            return endRow;
        }

        public void setEndRow(String endRow) {
            this.endRow = endRow;
        }

        public String getStartColumn() {
            return startColumn;
        }

        public void setStartColumn(String startColumn) {
            this.startColumn = startColumn;
        }

        public String getEndColumn() {
            return endColumn;
        }

        public void setEndColumn(String endColumn) {
            this.endColumn = endColumn;
        }

        public TableLocation(String sheet, String startRow, String endRow, String startColumn, String endColumn) {
            this.sheet = sheet;
            this.startRow = startRow;
            this.endRow = endRow;
            this.startColumn = startColumn;
            this.endColumn = endColumn;
        }

        @Override
        public String toString() {
            return String.format("%s!%s%s-%s%s",
                    sheet, startColumn, startRow, endColumn, endRow);
        }
    }

    /**
     * 添加表格位置信息
     */
    public void addTableLocation(String sheet, String startRow, String endRow, String startColumn, String endColumn) {
        TableLocation location = new TableLocation(sheet, startRow, endRow, startColumn, endColumn);
        locations.add(location);
    }

    /**
     * 添加位置信息（向后兼容的方法）
     */
    public void addLocation(String sheet, String row, String column) {
        // 简单处理：将单元格位置视为表格位置的一部分
        TableLocation location = new TableLocation(sheet, row, row, column, column);
        locations.add(location);
    }

    /**
     * 解析定位信息字符串，格式：(表名, 起始行-结束行, 起始列-结束列)
     * 列范围支持Excel格式的列标识(A,B,C)或数字标识(1,2,3)
     */
    public void parseLocationInfo(String locationInfo) {
        try {
            if (locationInfo == null || locationInfo.trim().isEmpty() || !locationInfo.startsWith("(") || !locationInfo.endsWith(")")) {
                // 无效的位置信息格式，使用默认值
                addTableLocation("默认表", "1", "1", "A", "A");
                return;
            }

            // 去除括号并分割
            String content = locationInfo.substring(1, locationInfo.length() - 1);
            String[] parts = content.split(", ");

            if (parts.length >= 3) {
                String tableName = parts[0];
                if (tableName == null || tableName.trim().isEmpty()) {
                    tableName = "默认表";
                }

                // 解析行范围
                String[] rowRange = parts[1].split("-");
                String startRow = rowRange[0];
                String endRow = rowRange.length > 1 ? rowRange[1] : startRow;

                // 解析列范围
                String[] colRange = parts[2].split("-");
                String startCol = colRange[0];
                String endCol = colRange.length > 1 ? colRange[1] : startCol;

                // 如果列是数字格式，转换为字母格式
                if (startCol.matches("\\d+")) {
                    startCol = convertNumberToExcelColumn(Integer.parseInt(startCol));
                }

                if (endCol.matches("\\d+")) {
                    endCol = convertNumberToExcelColumn(Integer.parseInt(endCol));
                }

                // 添加整个表格的位置信息
                addTableLocation(tableName, startRow, endRow, startCol, endCol);
            } else {
                // 格式不完整，至少添加基本定位信息
                String tableName = parts.length > 0 ? parts[0] : "默认表";
                if (tableName == null || tableName.trim().isEmpty()) {
                    tableName = "默认表";
                }
                addTableLocation(tableName, "1", "1", "A", "A");
            }
        } catch (Exception e) {
            // 如果解析失败，添加一个具有意义的默认定位点
            addTableLocation("默认表", "1", "1", "A", "A");
        }
    }

    /**
     * 将Excel列字母表示转换为数字
     * 例如：A -> 1, B -> 2, Z -> 26, AA -> 27
     * @param column 列字母
     * @return 列数字
     */
    public int convertExcelColumnToNumber(String column) {
        int result = 0;
        for (int i = 0; i < column.length(); i++) {
            result = result * 26 + (Character.toUpperCase(column.charAt(i)) - 'A' + 1);
        }
        return result;
    }

    /**
     * 将数字转换为Excel列字母表示
     * 例如：1 -> A, 2 -> B, 26 -> Z, 27 -> AA
     * @param number 列数字
     * @return 列字母
     */
    public String convertNumberToExcelColumn(int number) {
        StringBuilder result = new StringBuilder();
        while (number > 0) {
            int remainder = (number - 1) % 26;
            result.insert(0, (char) ('A' + remainder));
            number = (number - 1) / 26;
        }
        return result.length() > 0 ? result.toString() : "A";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locations.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(locations.get(i).toString());
        }
        return sb.toString();
    }
}