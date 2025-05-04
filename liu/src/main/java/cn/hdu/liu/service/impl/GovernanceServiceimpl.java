package cn.hdu.liu.service.impl;
import cn.hdu.liu.mapper.GovernanceMapper;
import cn.hdu.liu.obj.DataObject;
import cn.hdu.liu.service.GovernanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GovernanceServiceimpl implements GovernanceService {
    @Autowired
    private GovernanceMapper GovernanceMapper;
    @Override
    public DataObject search(String id) {
        return GovernanceMapper.search(id);
    }
}
