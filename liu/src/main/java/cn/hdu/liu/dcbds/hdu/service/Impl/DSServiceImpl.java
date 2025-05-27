package cn.hdu.liu.dcbds.hdu.service.Impl;

import cn.hutool.json.JSONObject;
import cn.hdu.liu.dcbds.hdu.Entity.SCR;
import cn.hdu.liu.dcbds.hdu.Entity.Sm2KeyEntity;
import cn.hdu.liu.dcbds.hdu.Entity.Voucher;
import cn.hdu.liu.dcbds.hdu.Utils.Common;
import cn.hdu.liu.dcbds.hdu.Utils.Sm2Utils;
import cn.hdu.liu.dcbds.hdu.Utils.Sm3Utils;
import cn.hdu.liu.dcbds.hdu.bswabe.BswabePub;
import cn.hdu.liu.dcbds.hdu.service.DSService;
import cn.hdu.liu.dcbds.hdu.service.VoucherService;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;

@Service
public class DSServiceImpl implements DSService {
    private final Sm2KeyEntity KeyPair;//数源方公私钥对
    private final String id;//数源方id

    public DSServiceImpl() throws Exception {
        this.KeyPair = Sm2Utils.generateSmKey();;
        this.id = Sm3Utils.encrypt(this.KeyPair.getPublicKey().getEncoded());
    }

    VoucherService voucherService = new VoucherServiceImpl();

    @Override
    public PublicKey getPublicKey() {
        return KeyPair.getPublicKey();
    }
    @Override
    public PrivateKey getPrivateKey() {
        return KeyPair.getPrivateKey(); // 返回私钥
    }
    @Override
    public String getId() {
        return id;
    }
    @Override
    public Voucher generateDV(BswabePub pub, String id, SCR scr, JSONObject rps, JSONObject hrp, JSONObject grp) throws Exception {
        String Did = Sm3Utils.encrypt(scr.getMetaData().getBytes());
        return voucherService.getVoucher(pub, id, Did, rps, hrp, grp, this.KeyPair.getPrivateKey());
    }
    @Override
    public JSONObject encryptAndSign(SCR scr, Voucher dv, PublicKey publicKey) throws Exception {
        JSONObject jsonObject = new JSONObject();

        byte[] scr_bytes = Common.serialize(scr);
        byte[] dv_bytes = Common.serialize(dv);
        byte[] content = Common.concat(scr_bytes, dv_bytes);

        byte[] C_Did = Sm2Utils.encrypt(content, publicKey);// 数据局公钥加密
        byte[] V_Did = Sm2Utils.signByPrivateKey(content, this.KeyPair.getPrivateKey());// 数源方私钥签名

        jsonObject.putOnce("C_Did", C_Did);
        jsonObject.putOnce("V_Did", V_Did);
        jsonObject.putOnce("scr_length", scr_bytes.length);
        jsonObject.putOnce("dv_length", dv_bytes.length);
        return jsonObject;
    }
}
