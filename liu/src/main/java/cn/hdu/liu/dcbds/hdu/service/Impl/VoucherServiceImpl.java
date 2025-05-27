package cn.hdu.liu.dcbds.hdu.service.Impl;

import cn.hutool.json.JSONObject;
import cn.hdu.liu.dcbds.hdu.Entity.Voucher;
import cn.hdu.liu.dcbds.hdu.Utils.Common;
import cn.hdu.liu.dcbds.hdu.Utils.Sm2Utils;
import cn.hdu.liu.dcbds.hdu.Utils.Sm3Utils;
import cn.hdu.liu.dcbds.hdu.bswabe.BswabePub;
import cn.hdu.liu.dcbds.hdu.service.VoucherService;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

@Service
public class VoucherServiceImpl implements VoucherService {
    @Override
    public Voucher getVoucher(BswabePub pub, String id, String Did, JSONObject rps, JSONObject hrp, JSONObject grp, PrivateKey sk) throws Exception {
        generateHRP(pub, new String[]{Did},rps,hrp);
        generateGRP(pub,new String[]{Did},rps,grp);

        String dig = Sm3Utils.encrypt(Common.concat(id.getBytes(), Did.getBytes()));
        byte[] sig = Sm2Utils.signByPrivateKey(dig.getBytes(), sk);
        return new Voucher(Did,hrp.getBytes(Did),grp.getBytes(Did),sig);
    }
    private void generateHRP(BswabePub pub, String[] attrs,JSONObject rps,JSONObject hrp) throws NoSuchAlgorithmException {

        Pairing pairing = pub.p;
        Element rp;
        for (String attr : attrs) {
            Element h_rp = pairing.getG2().newElement();
            elementFromString(h_rp, attr);
            rp = pairing.getZr().newElement();
            rp.setFromBytes(attr.getBytes());
            rps.putOnce(attr,rp.toBytes());
            h_rp.powZn(rp);
            hrp.putOnce(attr,h_rp.toBytes());
        }
    }
    private void generateGRP(BswabePub pub, String[] attrs,JSONObject rps,JSONObject grp) {

        Pairing pairing = pub.p;
        Element rp;
        for (String attr : attrs) {
            Element g_rp = pub.g.duplicate();
            rp = pairing.getZr().newElement();
            rp.setFromBytes(rps.getBytes(attr));
            g_rp.powZn(rp);
            grp.putOnce(attr,g_rp.toBytes());
        }
    }

    private  void elementFromString(Element h, String s)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(s.getBytes());
        h.setFromHash(digest, 0, digest.length);
    }
}
