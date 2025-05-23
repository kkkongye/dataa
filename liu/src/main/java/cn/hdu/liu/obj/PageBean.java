package cn.hdu.liu.obj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageBean {
    private long total;//总记录数
    private List rows;//数据列表
}
