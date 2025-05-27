package cn.hdu.liu.dcbds.hdu.Entity;

import java.io.Serializable;

public class BlockData implements Serializable {
    public final byte[] PK_U;// 访问者公钥

    public final byte[] PK_DB; // 接受方公钥

    public final String cid; // 胶囊id

    public final byte[] v; // 验证信息

    public final byte[] skr; // 访问这监管因子
    public final int visitTime; // 访问时间

    public BlockData (byte[] PK_U, byte[] PK_DB, String cid, byte[] v,byte[] skr, int visitTime) {
        this.PK_U = PK_U;
        this.PK_DB = PK_DB;
        this.cid = cid;
        this.v = v;
        this.skr = skr;
        this.visitTime = visitTime;
    }

    public byte[] getPK_U() {
        return PK_U;
    }

    public byte[] getPK_DB() {
        return PK_DB;
    }

    public String getCid() {
        return cid;
    }

    public byte[] getV() {
        return v;
    }
    public byte[] getSkr() {
        return skr;
    }

    public int getVisitTime() {
        return visitTime;
    }
}

