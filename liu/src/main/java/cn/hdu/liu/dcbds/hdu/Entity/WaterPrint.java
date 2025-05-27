package cn.hdu.liu.dcbds.hdu.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

public class WaterPrint implements Serializable {
    @JsonProperty("Cid")
    private  String Cid;
    private  Date expireTime;

    private  Integer visitTime;

    private  String idU;
    private  String idS;

    private  String idP;

    public WaterPrint(){}
    public WaterPrint(String Cid, Date expireTime, Integer visitTime, String idU, String idS, String idP) {
        this.Cid = Cid;
        this.expireTime = expireTime;
        this.visitTime = visitTime;
        this.idU = idU;
        this.idS = idS;
        this.idP = idP;
    }

    public String getCid() {
        return Cid;
    }
    public Date getExpireTime() {
        return expireTime;
    }
    public Integer getVisitTime() {
        return visitTime;
    }
    public String getIdU() {
        return idU;
    }
    public String getIdS() {
        return idS;
    }
    public String getIdP() {
        return idP;
    }

    public void setCid(String Cid) {
        this.Cid = Cid;
    }
    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }
    public void setVisitTime(Integer visitTime) {
        this.visitTime = visitTime;
    }
    public void setIdU(String idU) {
        this.idU = idU;
    }
    public void setIdS(String idS) {
        this.idS = idS;
    }
    public void setIdP(String idP) {
        this.idP = idP;
    }


}
