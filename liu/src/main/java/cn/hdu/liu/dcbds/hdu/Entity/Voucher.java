package cn.hdu.liu.dcbds.hdu.Entity;

import java.io.Serializable;

public class Voucher implements Serializable {
    private  String id;
    private  byte[] SK_ID;

    private  byte[] H_ID;

    private  byte[] V_ID;

    public Voucher(){}
    public Voucher(String id, byte[] SK_ID, byte[] H_ID, byte[] V_ID) {
        this.id = id;
        this.SK_ID = SK_ID;
        this.H_ID = H_ID;
        this.V_ID = V_ID;
    }

    public String getId() {
        return id;
    }

    public byte[] getSK_ID() {
        return SK_ID;
    }
    public byte[] getH_ID() {
        return H_ID;
    }

    public byte[] getV_ID() {
        return V_ID;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setSK_ID(byte[] SK_ID) {
        this.SK_ID = SK_ID;
    }
    public void setH_ID(byte[] H_ID) {
        this.H_ID = H_ID;
    }
    public void setV_ID(byte[] V_ID) {
        this.V_ID = V_ID;
    }


}
