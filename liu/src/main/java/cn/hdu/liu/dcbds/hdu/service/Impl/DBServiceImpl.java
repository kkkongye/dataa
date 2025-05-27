package cn.hdu.liu.dcbds.hdu.service.Impl;

import cn.hutool.json.JSONObject;
import cn.hdu.liu.dcbds.hdu.Entity.DataCapsule;
import cn.hdu.liu.dcbds.hdu.Entity.SCR;
import cn.hdu.liu.dcbds.hdu.Entity.Sm2KeyEntity;
import cn.hdu.liu.dcbds.hdu.Entity.Voucher;
import cn.hdu.liu.dcbds.hdu.Utils.*;
import cn.hdu.liu.dcbds.hdu.bswabe.*;
import cn.hdu.liu.dcbds.hdu.service.DBService;
import cn.hdu.liu.dcbds.hdu.service.Impl.VoucherServiceImpl;
import cn.hdu.liu.dcbds.hdu.service.VoucherService;
import it.unisa.dia.gas.jpbc.CurveParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.DefaultCurveParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.util.ArrayList;

@Service
public class DBServiceImpl implements DBService {
    private final Sm2KeyEntity KeyPair;// 数据局公私钥对
    private final String id;//数据局id
    private BswabeMsk msk;//系统秘密参数
    VoucherService voucherService = new VoucherServiceImpl();
    public DBServiceImpl() throws Exception {
        this.KeyPair = Sm2Utils.generateSmKey();;
        this.id = Sm3Utils.encrypt(this.KeyPair.getPublicKey().getEncoded());
    }
    @Override
    public PublicKey getPublicKey() {
        return KeyPair.getPublicKey();
    }
    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setup(BswabePub pub) {
        Element alpha, beta_inv;
        BswabeMsk msk = new BswabeMsk();
        String curveParams = "type a\n"
                + "q 87807107996633125224377819847540498158068831994142082"
                + "1102865339926647563088022295707862517942266222142315585"
                + "8769582317459277713367317481324925129998224791\n"
                + "h 12016012264891146079388821366740534204802954401251311"
                + "822919615131047207289359704531102844802183906537786776\n"
                + "r 730750818665451621361119245571504901405976559617\n"
                + "exp2 159\n" + "exp1 107\n" + "sign1 1\n" + "sign0 1\n";
        CurveParameters params = new DefaultCurveParameters()
                .load(new ByteArrayInputStream(curveParams.getBytes()));

        pub.pairingDesc = curveParams;
        pub.p = PairingFactory.getPairing(params);
        Pairing pairing = pub.p;

        pub.g = pairing.getG1().newElement();
        pub.f = pairing.getG1().newElement();
        pub.h = pairing.getG1().newElement();
        pub.gp = pairing.getG2().newElement();
        pub.g_hat_alpha = pairing.getGT().newElement();
        alpha = pairing.getZr().newElement();
        msk.beta = pairing.getZr().newElement();
        msk.g_alpha = pairing.getG2().newElement();

        alpha.setToRandom();
        msk.beta.setToRandom();
        pub.g.setToRandom();
        pub.gp.setToRandom();

        msk.g_alpha = pub.gp.duplicate();
        msk.g_alpha.powZn(alpha);

        beta_inv = msk.beta.duplicate(); // 副本
        beta_inv.invert();
        pub.f = pub.g.duplicate();
        pub.f.powZn(beta_inv);

        pub.h = pub.g.duplicate();
        pub.h.powZn(msk.beta);

        pub.g_hat_alpha = pairing.pairing(pub.g, msk.g_alpha); // e(g,g)^alpha
        this.msk = msk;
    }
    private BswabePrv keygen(BswabePub pub, BswabeMsk msk, String[] attrs, byte[] userGR, JSONObject hrp, JSONObject grp) {
        BswabePrv prv = new BswabePrv();
        Element g_r, r, beta_inv;
        Pairing pairing;

        /* initialize */
        pairing = pub.p;
        prv.d = pairing.getG2().newElement();
        g_r = pairing.getG2().newElement();
        r = pairing.getZr().newElement();
        beta_inv = pairing.getZr().newElement();

        /* compute */
        //r.setToRandom();
        // 监管因子
        g_r.setFromBytes(userGR);

        prv.d = msk.g_alpha.duplicate();
        prv.d.mul(g_r);
        beta_inv = msk.beta.duplicate();
        beta_inv.invert();
        prv.d.powZn(beta_inv);

        prv.gr = g_r.duplicate();

        int i, len = attrs.length;
        prv.comps = new ArrayList<BswabePrvComp>();
        for (i = 0; i < len; i++) {
            BswabePrvComp comp = new BswabePrvComp();
            Element h_rp;
            Element rp;

            comp.attr = attrs[i];

            comp.d = pairing.getG2().newElement();
            comp.dp = pairing.getG1().newElement();
            h_rp = pairing.getG2().newElement();
            rp = pairing.getZr().newElement();

//            elementFromString(h_rp, comp.attr);
            //rp.setToRandom();
            //获取对应的rj
//            byte[] rp_bytes = attrPub.getBytes(comp.attr);
//            rp.setFromBytes(rp_bytes);
//
//            h_rp.powZn(rp);
            h_rp.setFromBytes(hrp.getBytes(comp.attr));
            comp.d = g_r.duplicate();
            comp.d.mul(h_rp);
//            comp.dp = pub.g.duplicate();
//            comp.dp.powZn(rp);
            comp.dp.setFromBytes(grp.getBytes(comp.attr));
            comp.h_rp = h_rp.duplicate();
            prv.comps.add(comp);
        }

        return prv;
    }
    @Override
    public JSONObject decAndVerify(JSONObject cs, PublicKey publicKey) throws Exception {
        byte[] scr_dv = Sm2Utils.decrypt(cs.getBytes("C_Did"), this.KeyPair.getPrivateKey());
        boolean b = Sm2Utils.verifyByPublicKey(scr_dv, publicKey, cs.getBytes("V_Did"));
        if(!b){
            System.out.print("数据凭证解密失败或签名验证失败\n");
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        byte[] scr_bytes_db = new byte[cs.getInt("scr_length")];
        byte[] dv_bytes_db = new byte[cs.getInt("dv_length")];
        System.arraycopy(scr_dv,0, scr_bytes_db, 0, scr_bytes_db.length);
        System.arraycopy(scr_dv,scr_bytes_db.length, dv_bytes_db, 0, dv_bytes_db.length);
        SCR scr_db =(SCR) Common.deserialize(scr_bytes_db);
        Voucher dv_db = (Voucher) Common.deserialize(dv_bytes_db);
        jsonObject.putOnce("scr_db",scr_db);
        jsonObject.putOnce("dv_db",dv_db);
        return jsonObject;
    }
    @Override
    public Voucher generateOV(BswabePub pub, String id, String oid, JSONObject rps, JSONObject hrp, JSONObject grp) throws Exception {
        return voucherService.getVoucher(pub, id, oid, rps, hrp, grp, this.KeyPair.getPrivateKey());
    }
    @Override
    public JSONObject generateSC(BswabePub bswabePub, SCR scr, ArrayList<Voucher> vouchers, JSONObject grs, JSONObject hrp, JSONObject grp, PublicKey publicKey) throws Exception {
        // 计算监管因子
        Element r = bswabePub.p.getZr().newRandomElement();
        Element g_r = bswabePub.g.duplicate();
        g_r.mulZn(r);
        grs.putOnce(scr.getID_U(),g_r.toBytes());

        String[] vouchers_id = vouchers.stream().map(Voucher::getId).toArray(String[]::new);
        // 生成解密密钥
        BswabePrv sk = keygen(bswabePub, this.msk, vouchers_id, g_r.toBytes(), hrp, grp);

        byte[] dv_bytes = Common.serialize(vouchers.get(0));
        byte[] ov_bytes = Common.serialize(vouchers.get(1));
        byte[] sov_bytes = Common.serialize(vouchers.get(2));

        byte[] sk_bytes = SerializeUtils.serializeBswabePrv(sk);
        byte[] combined1 = Common.concat(dv_bytes, ov_bytes);
        byte[] combined2 = Common.concat(combined1, sov_bytes);
        byte[] sc = Common.concat(combined2, sk_bytes);
        // 加密并签名
        byte[] c_sc = Sm2Utils.encrypt(sc, publicKey);
        byte[] sig_sc = Sm2Utils.signByPrivateKey(sc, this.KeyPair.getPrivateKey());

        JSONObject jsonObject = new JSONObject();
        jsonObject.putOnce("c_sc", c_sc);
        jsonObject.putOnce("sig_sc", sig_sc);
        jsonObject.putOnce("dv_length", dv_bytes.length);
        jsonObject.putOnce("ov_length",ov_bytes.length);
        jsonObject.putOnce("sov_length",sov_bytes.length);
        jsonObject.putOnce("sk_length",sk_bytes.length);

        return jsonObject;
    }
    @Override
    public JSONObject regulate(BswabePub bswabePub, SCR scr, ArrayList<Voucher> vouchers, JSONObject grs, JSONObject hrp, JSONObject grp, DataCapsule dataCapsule) throws Exception {
        Element g_r = bswabePub.g.duplicate();
        g_r.setFromBytes(grs.getBytes(scr.getID_U()));
        String[] vouchers_id = vouchers.stream().map(Voucher::getId).toArray(String[]::new);

        BswabePrv sk = keygen(bswabePub, this.msk, vouchers_id, g_r.toBytes(), hrp, grp);


        CpAbe cpAbe = new CpAbe();
        byte[] cphBuf_du = dataCapsule.getCphBuf();
        byte[] sm4Buf_du = dataCapsule.getSm4Buf();
        BswabeCph cph = SerializeUtils.bswabeCphUnserialize(bswabePub, cphBuf_du);
        BswabeElementBoolean beb = cpAbe.dec(bswabePub, sk, cph);
        byte[] plt;
        if(beb.b){
            plt = Sm4Utils.decrypt_Ecb_Padding(beb.e.toBytes(), sm4Buf_du);
            if(plt!=null){
                System.out.println("解密成功!!!");
                System.out.println("解密的明文为: "+new String(plt));
                return new JSONObject().putOnce("plainText",plt);
            }
        }

        return null;
    }
}
