package cn.hdu.liu.service.impl;

import cn.hdu.liu.controller.SourceController;
import cn.hdu.liu.mapper.DataMapper;
import cn.hdu.liu.obj.*;
import cn.hdu.liu.service.DataObjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;



@Service
@Transactional
public class DataObjectServiceImpl implements DataObjectService {

    private final DataMapper dataMapper;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(DataObjectServiceImpl.class);

    public DataObjectServiceImpl(DataMapper dataMapper, ObjectMapper objectMapper) {
        this.dataMapper = dataMapper;
        this.objectMapper = objectMapper;
    }

    //---------------------- 核心方法 ----------------------
    @Override
    public boolean saveDataObject(DataObject dataObject) {
        try {
                calculateGrades(dataObject);
            // 确保Metadata的完整序列化
            if (dataObject.getDataEntity() != null && dataObject.getDataEntity().getMetadata() != null) {
                dataObject.setMetadataJson(objectMapper.writeValueAsString(dataObject.getDataEntity().getMetadata()));
            }

        } catch (Exception e) {
            throw new RuntimeException("保存数据对象失败", e);
        }

        try {
            // 手动处理对象到JSON的转换
            dataObject.setDataContent(objectMapper.writeValueAsString(dataObject.getDataEntity()));
            dataObject.setMetadataJson(objectMapper.writeValueAsString(dataObject.getDataEntity().getMetadata()));
            dataObject.setLocationInfoJson(objectMapper.writeValueAsString(dataObject.getLocationInfo()));
            dataObject.setConstraintSetJson(objectMapper.writeValueAsString(dataObject.getConstraintSet()));
            dataObject.setPropagationControlJson(objectMapper.writeValueAsString(dataObject.getPropagationControl()));
            dataObject.setAuditInfoJson(objectMapper.writeValueAsString(dataObject.getAuditInfo()));

            dataMapper.insert(dataObject);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("保存数据对象失败", e);
        }
    }

    @Override
    public DataObject findById(String id) {
        DataObject dataObject = dataMapper.selectById(id);
        if (dataObject != null) {
            try {
                // 反序列化基础字段
                dataObject.setDataEntity(objectMapper.readValue(dataObject.getDataContent(), DataEntity.class));
                dataObject.getDataEntity().setMetadata(objectMapper.readValue(dataObject.getMetadataJson(), Metadata.class));
                dataObject.setLocationInfo(objectMapper.readValue(dataObject.getLocationInfoJson(), LocationInfo.class));
                dataObject.setConstraintSet(objectMapper.readValue(dataObject.getConstraintSetJson(), ConstraintSet.class));
                dataObject.setPropagationControl(objectMapper.readValue(dataObject.getPropagationControlJson(), PropagationControl.class));
                dataObject.setAuditInfo(objectMapper.readValue(dataObject.getAuditInfoJson(), AuditInfo.class));

                // 反序列化新增分级字段
                if (dataObject.getDataEntity() != null) {
                    dataObject.setRowGrades(dataObject.getRowGradesJson());
                    dataObject.setColumnGrades(dataObject.getColumnGradesJson());
                }
            } catch (Exception e) {
                throw new RuntimeException("解析数据对象失败", e);
            }
        }
        return dataObject;
    }
    @Override
    public List<DataObject> findAll() {
        List<DataObject> list = dataMapper.selectAll();
        list.forEach(dataObject -> {
            try {
                // 反序列化基础字段
                dataObject.setDataEntity(objectMapper.readValue(dataObject.getDataContent(), DataEntity.class));
                dataObject.getDataEntity().setMetadata(objectMapper.readValue(dataObject.getMetadataJson(), Metadata.class));
                dataObject.setLocationInfo(objectMapper.readValue(dataObject.getLocationInfoJson(), LocationInfo.class));
                dataObject.setConstraintSet(objectMapper.readValue(dataObject.getConstraintSetJson(), ConstraintSet.class));
                dataObject.setPropagationControl(objectMapper.readValue(dataObject.getPropagationControlJson(), PropagationControl.class));
                dataObject.setAuditInfo(objectMapper.readValue(dataObject.getAuditInfoJson(), AuditInfo.class));


                if (dataObject.getDataEntity() != null) {
                    dataObject.setRowGrades(String.valueOf(new ArrayList<>(dataObject.getRowGrades())));
                    dataObject.setColumnGrades(String.valueOf(new ArrayList<>(dataObject.getColumnGrades())));
                }
            } catch (Exception e) {
                throw new RuntimeException("解析数据对象列表失败", e);
            }
        });
        return list;
    }


        // 获取表数量改为从Excel读取
        private int getTableCountFromExcel(InputStream excelStream, String fileName) {
            try (Workbook workbook = createWorkbook(excelStream, fileName)) {
                return workbook.getNumberOfSheets(); // 表数量即工作表数量
            } catch (Exception e) {
                log.error("读取Excel表数量失败", e);
                return 0;
            }
        }

        private Workbook createWorkbook(InputStream is, String fileName) throws IOException, IOException {
            if(fileName.toLowerCase().endsWith(".xlsx")) {
                return new XSSFWorkbook(is);
            } else if(fileName.toLowerCase().endsWith(".xls")) {
                return new HSSFWorkbook(is);
            }
            throw new IllegalArgumentException("不支持的Excel格式");
        }


    //---------------------- 辅助方法 ----------------------

    private void calculateGrades(DataObject dataObject) {
        try {
            DataEntity entity = dataObject.getDataEntity();
            if (entity == null) return;



            // 计算行分级值
            List<Double> rowGrades = new ArrayList<>();
            for (Map<String, String> row : entity.getDataItems()) {
                rowGrades.add(calculateRowGrade(row));
            }
            dataObject.setRowGrades(rowGrades.toString());



            //  计算表分级值
            dataObject.setTableGrade(calculateTableGrade(rowGrades));

        } catch (Exception e) {
            log.error("分级值计算失败", e);
        }
    }

    // 新增辅助方法
    private double calculateDatabaseGrade(int sheetCount) {
        if (sheetCount <= 10) return 100;
        else if (sheetCount <= 50) return 200;
        else return 300;
    }

    private double calculateRowGrade(Map<String, String> row) {
        double base = 1.0;
        boolean hasCore = false;
        boolean hasImportant = false;

        for (String value : row.values()) {
            if (value.contains("核心")) {
                hasCore = true;
            } else if (value.contains("重要")) {
                hasImportant = true;
            }
        }
        if (hasCore) {
            base = 3.0;
        } else if (hasImportant) {
            base = 2.0;
        }

        long validFields = row.values().stream().filter(v -> !v.isEmpty()).count();
        return base + validFields * 0.1;
    }

    private double calculateColumnGrade(String columnName) {
        if (columnName.contains("身份证")) return 0.8;
        if (columnName.contains("住址")) return 0.6;
        return 0.4;
    }

    private double calculateTableGrade(List<Double> rowGrades) {
        int rowCount = rowGrades.size();
        double sizeGrade = rowCount < 1000 ? 10 : rowCount < 1000000 ? 20 : 30;
        double maxRowGrade = rowGrades.stream()
                .mapToDouble(Double::doubleValue)
                .max().orElse(0);
        return sizeGrade + maxRowGrade;
    }




    @Override
    public List<DataObject> importFromExcelw(InputStream excelInputStream, String fileName,String origin,String id) {
        List<DataObject> dataObjects = new ArrayList<>();
        Workbook workbook = null;

        try {
            // 根据文件扩展名选择不同的Workbook实现
            if (fileName.toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(excelInputStream);
            } else if (fileName.toLowerCase().endsWith(".xls")) {
                workbook = new HSSFWorkbook(excelInputStream);
            } else {
                log.error("不支持的文件格式: {}", fileName);
                return dataObjects;
            }

            // 创建数据对象
            DataObject dataObject = new DataObject();

            int sheetCount = workbook.getNumberOfSheets();
            dataObject.setDbGrade(calculateDatabaseGrade(sheetCount));

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                log.error("Excel文件中没有工作表");
                return dataObjects;
            }

            // 获取表头行
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                log.error("Excel文件中没有表头行");
                return dataObjects;
            }

            // 检查表头是否为空
            boolean hasValidHeader = false;
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null && !getCellStringValue(cell).trim().isEmpty()) {
                    hasValidHeader = true;
                    break;
                }
            }

            if (!hasValidHeader) {
                log.error("Excel表头为空");
                return dataObjects;
            }

            // 表头映射 - 记录列索引
            Map<String, Integer> headerMap = new HashMap<>();
            Map<String, String> headerColumnMap = new HashMap<>(); // 记录列名和列索引的对应关系


            DataEntity dataEntity = new DataEntity();
            dataEntity.setEntity(origin);
            dataEntity.setStatus("待检验");
            dataEntity.setFeedback("");

            Metadata metadata = new Metadata();
            metadata.setHeaders(new ArrayList<>(headerMap.keySet()));
            dataEntity.setMetadata(metadata);

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String headerName = getCellStringValue(cell).trim();
                    if (!headerName.isEmpty()) {
                        // 检查列名是否重复
                        if (headerMap.containsKey(headerName)) {
                            log.warn("发现重复的列名: {}, 将使用新名称", headerName);
                            headerName = headerName + "_" + i;
                        }
                        headerMap.put(headerName, i);
                        // 记录列索引
                        String columnLetter = getColumnLetter(i);
                        headerColumnMap.put(headerName, columnLetter);
                    }
                }
            }

            List<Double> columnGrades = new ArrayList<>();
            for (String header : headerMap.keySet()) {
                double grade = calculateColumnGrade(header); // 调用计算方法
                columnGrades.add(grade);
                log.info("列名: {}，分级值: {}", header, grade);
            }
            dataObject.setColumnGrades(columnGrades.toString()); // 设置到DataObject

            if (headerMap.isEmpty()) {
                log.error("没有有效的表头列");
                return dataObjects;
            }

            log.info("检测到Excel表头: {}", headerMap.keySet());

            // 创建一个数据对象，用于存储整个Excel的数据
            String tableId = "table-" + fileName.replaceAll("[^a-zA-Z0-9]", "-");



            dataObject.setId(id);



            // 从第二行开始读取数据（跳过表头）
            int validRowCount = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // 检查行是否为空
                    boolean hasData = false;
                    for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null && !getCellStringValue(cell).trim().isEmpty()) {
                            hasData = true;
                            break;
                        }
                    }

                    if (!hasData) {
                        log.info("跳过空行: {}", i + 1);
                        continue;
                    }

                    // 每一行创建一个数据项
                    Map<String, String> dataItem = new HashMap<>();

                    // 为数据项添加行编号
                    dataItem.put("rowNumber", String.valueOf(i));

                    // 收集每行中的所有字段数据作为键值对
                    for (String header : headerMap.keySet()) {
                        int colIndex = headerMap.get(header);
                        Cell cell = row.getCell(colIndex);
                        String cellValue = getCellStringValue(cell);
                        if (cellValue != null && !cellValue.trim().isEmpty()) {
                            dataItem.put(header, cellValue.trim());
                        }
                    }

                    // 添加数据项到数据实体中
                    dataEntity.addDataItem(dataItem);
                    validRowCount++;

                    log.info("成功导入第{}行数据", i);
                } catch (Exception e) {
                    log.error("解析第{}行数据失败", i + 1, e);
                }
            }

            if (validRowCount == 0) {
                log.error("没有成功导入任何数据行");
                return dataObjects;
            }

            dataObject.setDataEntity(dataEntity);



            // 获取Excel文件名和工作表名
            String sheetName = workbook.getSheetName(0);
            if (sheetName == null || sheetName.trim().isEmpty()) {
                sheetName = "Sheet1";
            }

            // 使用Excel文件名和工作表名作为表标识
            // 去除文件扩展名
            String fileNameWithoutExt = fileName;
            if (fileName.toLowerCase().endsWith(".xlsx")) {
                fileNameWithoutExt = fileName.substring(0, fileName.length() - 5);
            } else if (fileName.toLowerCase().endsWith(".xls")) {
                fileNameWithoutExt = fileName.substring(0, fileName.length() - 4);
            }
            calculateGrades(dataObject);
            // 添加到结果列表
            dataObjects.add(dataObject);

            log.info("成功导入Excel数据，共{}行", validRowCount);

        } catch (Exception e) {
            log.error("导入Excel数据失败", e);
            throw new RuntimeException("导入Excel数据失败: " + e.getMessage(), e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    log.error("关闭工作簿失败", e);
                }
            }
        }

        return dataObjects;
    }



    public Map<String, String> getAvailablePropagationOperations() {
        // 返回传播控制选项
        Map<String, String> operations = new LinkedHashMap<>();
        operations.put("read", "可读");
        operations.put("modify", "可修改");
        operations.put("share", "可共享");
        operations.put("delegate", "可委托");
        operations.put("destroy", "可销毁");
        return operations;
    }

    @Override
    public void delete(String id) {
        dataMapper.delete(id);
    }

    @Override
    public void update(String id, DataObject dataObject) {
        // 1. 获取数据库中的现有对象
        DataObject existing = dataMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("ID为 " + id + " 的数据不存在");
        }

        // 2. 合并字段（仅更新非空字段）
        if (dataObject.getDataEntity() != null) {
            existing.setDataEntity(dataObject.getDataEntity());
        }
        if (dataObject.getLocationInfo() != null) {
            existing.setLocationInfo(dataObject.getLocationInfo());
        }
        if (dataObject.getConstraintSet() != null) {
            existing.setConstraintSet(dataObject.getConstraintSet());
        }
        if (dataObject.getPropagationControl() != null) {
            existing.setPropagationControl(dataObject.getPropagationControl());
        }
        if (dataObject.getAuditInfo() != null) {
            existing.setAuditInfo(dataObject.getAuditInfo());
        }

        // 3. 重新序列化对象到JSON字段（关键修改点）
        try {
            if (existing.getDataEntity() != null) {
                existing.setDataContent(objectMapper.writeValueAsString(existing.getDataEntity()));
                existing.setMetadataJson(objectMapper.writeValueAsString(existing.getDataEntity().getMetadata()));
            }
            if (existing.getLocationInfo() != null) {
                existing.setLocationInfoJson(objectMapper.writeValueAsString(existing.getLocationInfo()));
            }
            if (existing.getConstraintSet() != null) {
                existing.setConstraintSetJson(objectMapper.writeValueAsString(existing.getConstraintSet()));
            }
            if (existing.getPropagationControl() != null) {
                existing.setPropagationControlJson(objectMapper.writeValueAsString(existing.getPropagationControl()));
            }
            if (existing.getAuditInfo() != null) {
                existing.setAuditInfoJson(objectMapper.writeValueAsString(existing.getAuditInfo()));
            }
        } catch (Exception e) {
            throw new RuntimeException("序列化更新字段失败", e);
        }

        // 4. 触发更新
        existing.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        dataMapper.update(existing);  // 更新合并后的对象
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }


    private String getColumnLetter(int column) {
        // 单字母列（A-Z）
        if (column < 26) {
            return String.valueOf((char)('A' + column));
        }
        // 双字母列（AA-ZZ）
        else {
            int first = column / 26;
            int second = column % 26;
            return String.valueOf((char)('A' + first - 1)) + (char)('A' + second);
        }
    }


}