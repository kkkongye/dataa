package cn.hdu.liu.service.impl;

import cn.hdu.liu.service.DPService;
import cn.hdu.liu.utils.Sm4Utils;
import cn.hutool.json.JSONObject;
import com.hdu.Entity.DataCapsule;
import com.hdu.Entity.Sm2KeyEntity;
import com.hdu.Entity.Voucher;
import com.hdu.Entity.WaterPrint;
import com.hdu.Utils.Common;
import com.hdu.Utils.Sm2Utils;
import com.hdu.Utils.Sm3Utils;
import com.hdu.bswabe.*;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import cn.hdu.liu.obj.BswabePub;
import org.springframework.stereotype.Service;


@Service
public class DPServiceImpl implements DPService {

    private final Sm2KeyEntity KeyPair;//数源方公私钥对
    private final String id;//数源方id

    public DPServiceImpl() throws Exception {
        this.KeyPair = Sm2Utils.generateSmKey();
        this.id = Sm3Utils.encrypt(this.KeyPair.getPublicKey().getEncoded());
    }

    private BswabeCphKey enc(BswabePub pub, String policy) throws Exception {
        BswabeCphKey keyCph = new BswabeCphKey();
        BswabeCph cph = new BswabeCph();
        Element s, m;

        /* initialize */

        Pairing pairing = pub.p;
        s = pairing.getZr().newElement(); // 随机取一个s
        m = pairing.getGT().newElement();// 随机取一个m
        cph.cs = pairing.getGT().newElement();
        cph.c = pairing.getG1().newElement();
        cph.p = parsePolicyPostfix(pub,policy); // 初始化policy树

        /* compute */
        m.setToRandom();
        s.setToRandom();
        cph.cs = pub.g_hat_alpha.duplicate();
        cph.cs.powZn(s); /* num_exps++; */
        cph.cs.mul(m); /* num_muls++; */

        cph.c = pub.h.duplicate();
        cph.c.powZn(s); /* num_exps++; */ // h^S

        fillPolicy(cph.p, pub, s); // 把s藏入policy树当中

        keyCph.cph = cph;
        keyCph.key = m;

        return keyCph;
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
    public JSONObject decAnyVerify(JSONObject req, PublicKey publicKey, PublicKey publicKey1) throws Exception {

        byte[] dv_bytes = new byte[req.getInt("dv_length")];
        byte[] ov_bytes = new byte[req.getInt("ov_length")];
        byte[] sov_bytes = new byte[req.getInt("sov_length")];
        byte[] MetaData_bytes = new byte[req.getInt("MetaData_length")];
        byte[] req_bytes = req.getBytes("req");
        System.arraycopy(req_bytes,0, dv_bytes, 0, dv_bytes.length);
        System.arraycopy(req_bytes,dv_bytes.length, ov_bytes, 0, ov_bytes.length);
        System.arraycopy(req_bytes,dv_bytes.length+ov_bytes.length, sov_bytes, 0, sov_bytes.length);
        System.arraycopy(req_bytes,dv_bytes.length+ov_bytes.length+sov_bytes.length, MetaData_bytes, 0, MetaData_bytes.length);

        Voucher dv_dp = (Voucher) Common.deserialize(dv_bytes);
        Voucher ov_dp = (Voucher) Common.deserialize(ov_bytes);
        Voucher sov_dp = (Voucher) Common.deserialize(sov_bytes);
        String MetaData_dp = new String(MetaData_bytes);

        // 验证凭证
        System.out.println("数据平台开始验证凭证...................................................");
        String dv_v_dig = Sm3Utils.encrypt(Common.concat(req.getStr("ID_U").getBytes(),dv_dp.getId().getBytes()));
        String ov_v_dig = Sm3Utils.encrypt(Common.concat(req.getStr("ID_U").getBytes(),ov_dp.getId().getBytes()));
        String sov_v_dig = Sm3Utils.encrypt(Common.concat(req.getStr("ID_SU").getBytes(),sov_dp.getId().getBytes()));


        boolean b1 = Sm2Utils.verifyByPublicKey(dv_v_dig.getBytes(), publicKey, dv_dp.getV_ID());
        boolean b3 = Sm2Utils.verifyByPublicKey(ov_v_dig.getBytes(),publicKey1, ov_dp.getV_ID());
        boolean b4 = Sm2Utils.verifyByPublicKey(sov_v_dig.getBytes(), publicKey1, sov_dp.getV_ID());
        if(!b1 || !b3 || !b4){
            System.out.print("凭证验证失败\n");
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOnce("dv_dp",dv_dp);
        jsonObject.putOnce("ov_dp",ov_dp);
        jsonObject.putOnce("sov_dp",sov_dp);
        return jsonObject;
    }

    @Override
    public DataCapsule encapsulate(BswabePub bswabePub, byte[] plainText, Date timeExpire, int visitTime,String policy,String[] ids) throws Exception {
        BswabeCphKey keyCph = enc(bswabePub, policy);
        byte[] cphBuf = SerializeUtils.bswabeCphSerialize(keyCph.cph);
        String cid = Sm3Utils.encrypt(cphBuf);

        byte[] sm4Buf = Sm4Utils.encrypt_Ecb_Padding(keyCph.key.toBytes(), plainText);
        byte[] v = Common.concat(Common.concat(Common.concat(Common.concat(cid.getBytes(),cphBuf),sm4Buf),SerializeUtils.serializeLong64(timeExpire.getTime())),SerializeUtils.serializeUint32(visitTime));
        Element rv = bswabePub.p.getZr().newRandomElement();
        rv.setFromBytes(v);
        Element V = bswabePub.g.duplicate();
        V.powZn(rv);
        WaterPrint wp = new WaterPrint(cid,timeExpire,visitTime,ids[0],ids[1],ids[2]);
        // 封装胶囊
        return new DataCapsule(cid, cphBuf, sm4Buf,V.toBytes(), wp);
    }

    private BswabePolicy parsePolicyPostfix(BswabePub pub, String s)  {
        String[] toks;
        String tok;
        ArrayList<BswabePolicy> stack = new ArrayList<>();
        BswabePolicy root;

        toks = s.split(" ");// 按照空格进行split

        int toks_cnt = toks.length;
        for (int index = 0; index < toks_cnt; index++) {
            int i, k, n;

            tok = toks[index];
            if (!tok.contains("of")) {
                stack.add(baseNode(pub,1, tok));
            } else {
                BswabePolicy node;

                /* parse kof n node */
                String[] k_n = tok.split("of");
                k = Integer.parseInt(k_n[0]);
                n = Integer.parseInt(k_n[1]);

                if (k < 1) {
                    System.out.println("error parsing " + s
                            + ": trivially satisfied operator " + tok);
                    return null;
                } else if (k > n) {
                    System.out.println("error parsing " + s
                            + ": unsatisfiable operator " + tok);
                    return null;
                } else if (n == 1) {
                    System.out.println("error parsing " + s
                            + ": indentity operator " + tok);
                    return null;
                } else if (n > stack.size()) {
                    System.out.println("error parsing " + s
                            + ": stack underflow at " + tok);
                    return null;
                }

                /* pop n things and fill in children */
                node = baseNode(pub,k, null);
                node.children = new BswabePolicy[n];

                for (i = n - 1; i >= 0; i--)
                    node.children[i] = stack.remove(stack.size() - 1);

                /* push result */
                stack.add(node);
            }
        }

        if (stack.size() > 1) {
            System.out.println("error parsing " + s
                    + ": extra node left on the stack");
            return null;
        } else if (stack.size() < 1) {
            System.out.println("error parsing " + s + ": empty policy");
            return null;
        }

        root = stack.get(0);
        return root;
    }
    private void fillPolicy(BswabePolicy p, BswabePub pub, Element e)
            throws NoSuchAlgorithmException {
        int i;
        Element r, t, h;
        Pairing pairing = pub.p;
        r = pairing.getZr().newElement();
        t = pairing.getZr().newElement();
        h = pairing.getG2().newElement();

        p.q = randPoly(p.k - 1, e); // 常数项为e的k-1次多项式

        if (p.children == null || p.children.length == 0) { // 叶子节点
            p.c = pairing.getG1().newElement();
            p.cp = pairing.getG2().newElement();

            elementFromString(h, p.attr); //对attr进行hash
            p.c = pub.g.duplicate();
            p.c.powZn(p.q.coef[0]);
            p.cp = h.duplicate();
            p.cp.powZn(p.q.coef[0]);
//            p.cp = pairing.pairing(p.gri, p.cp); // cp = e(g,g)^q(0)
        } else {
            for (i = 0; i < p.children.length; i++) {
                r.set(i + 1);
                evalPoly(t, p.q, r); // 求q_x(i)
                fillPolicy(p.children[i], pub, t);
            }
        }
    }

    private void evalPoly(Element r, BswabePolynomial q, Element x) {
        int i;
        Element s, t;

        s = r.duplicate();
        t = r.duplicate();

        r.setToZero();
        t.setToOne();

        for (i = 0; i < q.deg + 1; i++) {
            /* r += q->coef[i] * t */
            s = q.coef[i].duplicate();
            s.mul(t);
            r.add(s);

            /* t *= x */
            t.mul(x);
        }

    }

    private  void elementFromString(Element h, String s)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(s.getBytes());
        h.setFromHash(digest, 0, digest.length);
    }

    private BswabePolynomial randPoly(int deg, Element zeroVal) {
        int i;
        BswabePolynomial q = new BswabePolynomial();
        q.deg = deg;
        q.coef = new Element[deg + 1];

        for (i = 0; i < deg + 1; i++)
            q.coef[i] = zeroVal.duplicate();

        q.coef[0].set(zeroVal);

        for (i = 1; i < deg + 1; i++)
            q.coef[i].setToRandom();

        return q;
    }

    private BswabePolicy baseNode(BswabePub bswabePub,int k, String s) {
        BswabePolicy p = new BswabePolicy();

//        if(s!=null){
//            p.gri = bswabePub.p.getG1().newElement();
//            p.gri.setFromBytes(attrJson.get(s,byte[].class));
//        }

        p.k = k;
        if (!(s == null))
            p.attr = s;
        else
            p.attr = null;
        p.q = null; // 初始化时多项式系数为null

        return p;
    }
}
