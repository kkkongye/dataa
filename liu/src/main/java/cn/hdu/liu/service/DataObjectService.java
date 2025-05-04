package cn.hdu.liu.service;

import cn.hdu.liu.obj.DataObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


public interface DataObjectService {
    List<DataObject> importFromExcelw(InputStream excelInputStream, String fileName,String origin,String id);
    default DataObject importFromExcel(String filePath,String origin,String id) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("文件不存在或不是有效的文件: " + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            List<DataObject> results = importFromExcelw(fis, file.getName(),origin,id);
            return results.isEmpty() ? null : results.get(0);
        }
    }


    boolean saveDataObject(DataObject dataObject);
    List<DataObject> findAll();
    DataObject findById(String id);
    void update(String id, DataObject dataObject);
    void delete(String id);
}