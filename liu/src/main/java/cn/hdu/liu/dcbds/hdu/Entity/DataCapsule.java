package cn.hdu.liu.dcbds.hdu.Entity;

import cn.hdu.liu.dcbds.hdu.Entity.WaterPrint;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class DataCapsule implements Serializable {
    @JsonProperty("Cid")
    private  String Cid;
    private  byte[] cphBuf;
    private  byte[] sm4Buf;

    private  byte[] v;

    private cn.hdu.liu.dcbds.hdu.Entity.WaterPrint wp;

    public DataCapsule(){}
    public DataCapsule(String Cid, byte[] cphBuf, byte[] sm4Buf, byte[] v, cn.hdu.liu.dcbds.hdu.Entity.WaterPrint wp) {
        this.Cid = Cid;
        this.cphBuf = cphBuf;
        this.sm4Buf = sm4Buf;
        this.v = v;
        this.wp = wp;
    }

    public byte[] getCphBuf() {
        return cphBuf;
    }
    public byte[] getSm4Buf() {
        return sm4Buf;
    }
    public byte[] getV() {
        return v;
    }
    public cn.hdu.liu.dcbds.hdu.Entity.WaterPrint getWp() {
        return wp;
    }
    public String getCid() {
        return Cid;
    }

    public void setCphBuf(byte[] cphBuf) {
        this.cphBuf = cphBuf;
    }
    public void setSm4Buf(byte[] sm4Buf) {
        this.sm4Buf = sm4Buf;
    }
    public void setV(byte[] v) {
        this.v = v;
    }
    public void setWp(WaterPrint wp) {
        this.wp = wp;
    }
    public void setCid(String Cid) {
        this.Cid = Cid;
    }


}
