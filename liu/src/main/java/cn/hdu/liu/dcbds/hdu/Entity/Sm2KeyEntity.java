package cn.hdu.liu.dcbds.hdu.Entity;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Sm2KeyEntity implements Serializable {

    private  PublicKey publicKey;
    private  PrivateKey privateKey;

    public Sm2KeyEntity(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public Sm2KeyEntity() {
    }
    public PublicKey getPublicKey() {
        return publicKey;
    }
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }
}
