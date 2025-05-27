package cn.hdu.liu.dcbds.hdu.service;

import cn.hutool.json.JSONObject;
import cn.hdu.liu.dcbds.hdu.Entity.SCR;
import cn.hdu.liu.dcbds.hdu.Entity.Voucher;
import cn.hdu.liu.dcbds.hdu.bswabe.BswabePub;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface DSService {

    PrivateKey getPrivateKey();
    /**
     * 获取数源方公钥
     *
     * @return 数源方公钥
     */
    PublicKey getPublicKey();

    /**
     * 获取数源方ID
     * @return 数源方ID
     */
    String getId();

    /**
     * 生成数据凭证
     * @param pub 系统公开参数
     * @param id 需求方ID
     * @param scr 共享证书请求
     * @param rps 系统参数表
     * @param hrp 系统参数表
     * @param grp 系统参数表
     * @return 数据凭证
     * @throws Exception
     */
    Voucher generateDV(BswabePub pub, String id, SCR scr, JSONObject rps, JSONObject hrp, JSONObject grp) throws Exception;

    /**
     * 数据凭证加密并签名
     * @param scr 共享证书请求
     * @param dv 数据凭证
     * @param publicKey 数据局公钥
     * @return 加密并签名后的数据
     * @throws Exception
     */
    JSONObject encryptAndSign(SCR scr, Voucher dv, PublicKey publicKey) throws Exception;
}
