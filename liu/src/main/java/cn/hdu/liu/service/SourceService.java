package cn.hdu.liu.service;

import cn.hdu.liu.obj.DataObject;

import java.util.List;

public interface SourceService {

    void delete(Integer id);

    DataObject search(Integer id);

    void update(Integer id, DataObject dataObject);

    void add(DataObject dataObject);

    List<DataObject> list();
}
