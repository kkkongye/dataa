package cn.hdu.liu.dcbds.hdu.Entity;

import java.io.Serializable;

public class SCR implements Serializable {
    /**
     * 共享证书请求类
     */
    private  String MetaData;

    private  String FNO;
    private  String SFNO;
    private  String ID_U;
    private  String ID_SDU;

    public SCR(){}
    public SCR(String MetaData, String FNO, String SFNO, String ID_U, String ID_SDU) {
        this.MetaData = MetaData;
        this.FNO = FNO;
        this.SFNO = SFNO;
        this.ID_U = ID_U;
        this.ID_SDU = ID_SDU;
    }

    public String getMetaData() {
        return MetaData;
    }
    public String getFNO() {
        return FNO;
    }
    public String getSFNO() {
        return SFNO;
    }
    public String getID_U() {
        return ID_U;
    }
    public String getID_SDU() {
        return ID_SDU;
    }

    public void setMetaData(String MetaData) {
        this.MetaData = MetaData;
    }
    public void setFNO(String FNO) {
        this.FNO = FNO;
    }
    public void setSFNO(String SFNO) {
        this.SFNO = SFNO;
    }
    public void setID_U(String ID_U) {
        this.ID_U = ID_U;
    }
    public void setID_SDU(String ID_SDU) {
        this.ID_SDU = ID_SDU;
    }

}
