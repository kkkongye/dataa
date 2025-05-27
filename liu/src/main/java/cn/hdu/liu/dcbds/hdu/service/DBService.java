package cn.hdu.liu.dcbds.hdu.service;

import cn.hutool.json.JSONObject;
import cn.hdu.liu.dcbds.hdu.Entity.DataCapsule;
import cn.hdu.liu.dcbds.hdu.Entity.SCR;
import cn.hdu.liu.dcbds.hdu.Entity.Voucher;
import cn.hdu.liu.dcbds.hdu.bswabe.BswabePub;

import java.security.PublicKey;
import java.util.ArrayList;

public interface DBService {


    /**
     * 获取数据局公钥
     * @return 数据局公钥
     */
    PublicKey getPublicKey();

    /**
     * 获取数据局ID
     * @return 数据局ID
     */
    String getId();

    /**
     * 数据局执行系统初始化
     * @param pub 系统公开参数
     */
    void setup(BswabePub pub);

    /**
     * 数据局验证数据凭证
     * @param cs 加密并签名后的数据凭证
     * @param publicKey 数源方公钥
     * @return 共享证书请求scr和数据凭证dv
     * @throws Exception
     */
    JSONObject decAndVerify(JSONObject cs, PublicKey publicKey) throws Exception;

    /**
     * 生成组织机构凭证
     * @param pub 系统公开参数
     * @param id 用户ID
     * @param oid 组织机构ID
     * @param rps 系统参数表
     * @param hrp 系统参数表
     * @param grp 系统参数表
     * @return 组织机构凭证
     * @throws Exception
     */
    Voucher generateOV(BswabePub pub, String id, String oid, JSONObject rps, JSONObject hrp, JSONObject grp) throws Exception;

    /**
     * 生成共享证书
     *
     * @param bswabePub 系统公开参数
     * @param scr       共享证书请求
     * @param vouchers  所有凭证
     * @param grs       系统参数表
     * @param hrp       系统参数表
     * @param grp       系统参数表
     * @param publicKey 数据局公钥
     * @return 共享证书
     * @throws Exception
     */
    JSONObject generateSC(BswabePub bswabePub,SCR scr, ArrayList<Voucher> vouchers,JSONObject grs,JSONObject hrp,JSONObject grp,PublicKey publicKey) throws Exception;


    /**
     * 数据局监管数据
     * @param bswabePub 系统公开参数
     * @param scr 共享证书请求
     * @param vouchers 所有凭证
     * @param grs 系统参数表
     * @param hrp 系统参数表
     * @param grp 系统参数表
     * @param dataCapsule 数据胶囊
     * @return 监管信息
     * @throws Exception
     */
    JSONObject regulate(BswabePub bswabePub, SCR scr, ArrayList<Voucher> vouchers, JSONObject grs, JSONObject hrp, JSONObject grp, DataCapsule dataCapsule) throws Exception;
}
