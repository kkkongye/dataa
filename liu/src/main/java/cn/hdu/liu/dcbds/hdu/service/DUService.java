package cn.hdu.liu.dcbds.hdu.service;

import cn.hutool.json.JSONObject;
import cn.hdu.liu.dcbds.hdu.Entity.Blockchain;
import cn.hdu.liu.dcbds.hdu.Entity.DataCapsule;
import cn.hdu.liu.dcbds.hdu.Entity.SCR;
import cn.hdu.liu.dcbds.hdu.bswabe.BswabePub;

import java.io.IOException;
import java.security.PublicKey;

public interface DUService {
    /**
     * 获取公共数据需求方公钥
     * @return 公共数据需求方公钥
     */
    PublicKey getPublicKey();
    /**
     * 获取公共数据需求方ID
     * @return 公共数据需求方ID
     */
    String getId();
    /**
     * 解封装胶囊
     * @param bswabePub 系统公开参数
     * @param dataCapsule 数据胶囊

     * @param dupublicKey 需求方公钥
     * @param dbPublicKey 数据局公钥
     * @Return 解封装后的数据
     * @throws Exception
     */
    JSONObject decryptionCapsule(BswabePub bswabePub, DataCapsule dataCapsule,  PublicKey dupublicKey, PublicKey dbPublicKey) throws Exception;
    /**
     * 生成数据请求
     * @param MetaData 元数据
     * @param FNO 组织机构全称
     * @param SFNO 上级组织机构全称
     * @param ID_U 需求方ID
     * @param ID_SDU 上级ID
     * @return SCR
     */
    SCR generateSCR(String MetaData, String FNO, String SFNO, String ID_U, String ID_SDU);
    /**
     * 共享证书解密与验证
     * @param pub 系统公钥
     * @param sc 共享证书
     * @param publicKey 数据局公钥
     * @return 共享证书
     * @throws Exception
     */
    JSONObject decAndVerify(BswabePub pub, JSONObject sc, PublicKey publicKey) throws Exception;
    /**
     * 生成胶囊请求req
     * @param sc 共享证书
     * @param MetaData 元数据
     * @param ID_U 需求方ID
     * @param ID_SU 上级ID
     * @return 数据请求
     * @throws IOException
     */
    JSONObject generateReq(JSONObject sc,String MetaData,String ID_U,String ID_SU) throws IOException;

}
