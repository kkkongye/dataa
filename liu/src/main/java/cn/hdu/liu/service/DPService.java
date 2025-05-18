package cn.hdu.liu.service;

import cn.hdu.liu.obj.BswabePub;
import cn.hutool.json.JSONObject;
import com.hdu.Entity.DataCapsule;


import java.security.PublicKey;
import java.util.Date;

public interface DPService {

    /**
     * 获取公共数据平台公钥
     * @return 公共数据平台公钥
     */
    PublicKey getPublicKey();
    /**
     * 获取公共数据平台ID
     * @return 公共数据平台ID
     */
    String getId();

    /**
     * 验证所有凭证
     * @param req 数据请求
     * @param publicKey 数源方公钥
     * @param publicKey1 数据局公钥
     * @return 验证信息
     * @throws Exception
     */
    JSONObject decAnyVerify(JSONObject req, PublicKey publicKey,PublicKey publicKey1) throws Exception;

    /**
     * 封装胶囊
     * @param bswabePub 系统公开参数
     * @param plainText 明文
     * @param timeExpire 过期时间
     * @param visitTime  可访问次数
     * @param policy   访问策略
     * @param ids 用户ID
     * @return 数据胶囊
     * @throws Exception
     */
    DataCapsule encapsulate(BswabePub bswabePub, byte[] plainText, Date timeExpire, int visitTime, String policy, String[] ids) throws Exception;
}
