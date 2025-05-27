package cn.hdu.liu.dcbds.hdu.service.Impl;

import cn.hutool.json.JSONObject;
import cn.hdu.liu.dcbds.hdu.Entity.*;
import cn.hdu.liu.dcbds.hdu.Utils.*;
import cn.hdu.liu.dcbds.hdu.bswabe.*;
import cn.hdu.liu.dcbds.hdu.service.DUService;
import it.unisa.dia.gas.jpbc.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.PublicKey;


@Service
public class DUServiceImpl implements DUService{
    private BswabePrv sk;//解密密钥
    private final Sm2KeyEntity KeyPair;//需求方公私钥对
    private final String id;//ID

    public DUServiceImpl() throws Exception {
        this.KeyPair = Sm2Utils.generateSmKey();;
        this.id = Sm3Utils.encrypt(this.KeyPair.getPublicKey().getEncoded());


    }
    @Override
    public String getId() {
        return id;
    }
    @Override
    public PublicKey getPublicKey() {
        return KeyPair.getPublicKey();
    }
    @Override
    public JSONObject decryptionCapsule(BswabePub bswabePub, DataCapsule dataCapsule,  PublicKey dupublicKey, PublicKey dbPublicKey) throws Exception {
        String cid_u = dataCapsule.getCid();


        //没过期，继续解密
        byte[] cphBuf_du = dataCapsule.getCphBuf();
        byte[] sm4Buf_du = dataCapsule.getSm4Buf();
        BswabeCph cph = SerializeUtils.bswabeCphUnserialize(bswabePub, cphBuf_du);
        CpAbe cpAbe = new CpAbe();
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
    private boolean verifyDataCapsule(BswabePub bswabePub,DataCapsule dataCapsule, BlockData block){

        if(block == null){
            return false;
        }
        byte[] vp = Common.concat(Common.concat(Common.concat(Common.concat(dataCapsule.getCid().getBytes(),dataCapsule.getCphBuf()),dataCapsule.getSm4Buf()),SerializeUtils.serializeLong64(dataCapsule.getWp().getExpireTime().getTime())),SerializeUtils.serializeUint32(dataCapsule.getWp().getVisitTime()));
        Element rvp = bswabePub.p.getZr().newRandomElement();
        rvp.setFromBytes(vp);
        Element VP = bswabePub.g.duplicate();
        VP.powZn(rvp);

        Element VPP = bswabePub.p.getG2().newElement();
        VPP.setFromBytes(block.getV());
        Element VPPP = bswabePub.p.getG2().newElement();
        VPPP.setFromBytes(dataCapsule.getV());

        if(!VP.isEqual(VPP)||!VP.isEqual(VPPP) ){
            System.out.println("数据胶囊验证失败");
            return false;
        }
        if(dataCapsule.getWp().getExpireTime().getTime()<System.currentTimeMillis() || dataCapsule.getWp().getVisitTime()<=block.getVisitTime()){
            System.out.println("数据胶囊已过期");
            return false;
        }
        return true;
    }
    @Override
    public SCR generateSCR(String MetaData, String FNO, String SFNO, String ID_U, String ID_SDU) {
        return new SCR(MetaData, FNO, SFNO, ID_U, ID_SDU);
    }
    @Override
    public JSONObject decAndVerify(BswabePub pub, JSONObject sc, PublicKey publicKey) throws Exception {
        byte[] sc_bytes = Sm2Utils.decrypt(sc.getBytes("c_sc"),this.KeyPair.getPrivateKey());
        boolean b2 = Sm2Utils.verifyByPublicKey(sc_bytes, publicKey, sc.getBytes("sig_sc"));
        if(!b2){
            System.out.print("组织机构凭证解密失败或签名验证失败\n");
            return null;
        }
        byte[] dv_bytes = new byte[sc.getInt("dv_length")];
        byte[] ov_bytes = new byte[sc.getInt("ov_length")];
        byte[] sov_bytes = new byte[sc.getInt("sov_length")];
        byte[] sk_bytes =new byte[sc.getInt("sk_length")];
        System.arraycopy(sc_bytes,0, dv_bytes, 0, dv_bytes.length);
        System.arraycopy(sc_bytes,dv_bytes.length, ov_bytes, 0, ov_bytes.length);
        System.arraycopy(sc_bytes,dv_bytes.length+ov_bytes.length, sov_bytes, 0, sov_bytes.length);
        System.arraycopy(sc_bytes,dv_bytes.length+ov_bytes.length+sov_bytes.length, sk_bytes, 0, sk_bytes.length);
        this.sk = SerializeUtils.unserializeBswabePrv(pub, sk_bytes);

        Voucher dv = (Voucher) Common.deserialize(dv_bytes);
        Voucher ov = (Voucher) Common.deserialize(ov_bytes);
        Voucher sov = (Voucher) Common.deserialize(sov_bytes);


        JSONObject jsonObject = new JSONObject();
        jsonObject.putOnce("dv",dv);
        jsonObject.putOnce("ov",ov);
        jsonObject.putOnce("sov",sov);
        jsonObject.putOnce("sk_bytes",sk_bytes);

        return jsonObject;
    }
    @Override
    public JSONObject generateReq(JSONObject sc,String MetaData,String ID_U,String ID_SU) throws IOException {
        Voucher dv = sc.get("dv", Voucher.class);
        Voucher ov = sc.get("ov", Voucher.class);
        Voucher sov = sc.get("sov", Voucher.class);

        Voucher dv_to_dp = new Voucher(dv.getId(), null,null,dv.getV_ID());
        Voucher ov_to_dp = new Voucher(ov.getId(),null,null,ov.getV_ID());
        Voucher sov_to_dp = new Voucher(sov.getId(),null,null,sov.getV_ID());

        byte[] dv_to_dp_bytes = Common.serialize(dv_to_dp);
        byte[] ov_to_dp_bytes = Common.serialize(ov_to_dp);
        byte[] sov_to_dp_bytes = Common.serialize(sov_to_dp);

        byte[] content3 = Common.concat(dv_to_dp_bytes,ov_to_dp_bytes);
        byte[] content4 = Common.concat(content3, sov_to_dp_bytes);
        byte[] req = Common.concat(content4,MetaData.getBytes());

        JSONObject jsonObject = new JSONObject();
        jsonObject.putOnce("req",req);
        jsonObject.putOnce("dv_length",dv_to_dp_bytes.length);
        jsonObject.putOnce("ov_length",ov_to_dp_bytes.length);
        jsonObject.putOnce("sov_length",sov_to_dp_bytes.length);
        jsonObject.putOnce("MetaData_length",MetaData.length());
        jsonObject.putOnce("ID_U",ID_U);
        jsonObject.putOnce("ID_SU",ID_SU);
        return jsonObject;
    }
}
