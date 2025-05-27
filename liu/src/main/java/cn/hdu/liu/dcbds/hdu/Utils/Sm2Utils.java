package cn.hdu.liu.dcbds.hdu.Utils;
import cn.hdu.liu.dcbds.hdu.Entity.Sm2KeyEntity;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.util.encoders.Hex;

import java.security.*;
import java.security.spec.ECGenParameterSpec;

public class Sm2Utils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    private final static String CRYPTO_NAME_SM2 = "sm2p256v1";

    /**
     * 生成国密公私钥对
     *
     * @return Sm2国密对
     * @throws Exception
     */
    public static Sm2KeyEntity generateSmKey() throws Exception {
        KeyPairGenerator keyPairGenerator = null;
        SecureRandom secureRandom = new SecureRandom();
        ECGenParameterSpec sm2Spec = new ECGenParameterSpec(CRYPTO_NAME_SM2);
        keyPairGenerator = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
        keyPairGenerator.initialize(sm2Spec);
        keyPairGenerator.initialize(sm2Spec, secureRandom);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        return new Sm2KeyEntity(privateKey, publicKey);
    }


    /**
     * 根据publicKey对原始数据data，使用SM2加密
     */
    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws InvalidCipherTextException {

        BCECPublicKey localECPublicKey = (BCECPublicKey) publicKey;
        ECParameterSpec localECParameterSpec = localECPublicKey.getParameters();
        ECDomainParameters localECDomainParameters = new ECDomainParameters(
                localECParameterSpec.getCurve(), localECParameterSpec.getG(), localECParameterSpec.getN()
        );
        ECPublicKeyParameters localECPublicKeyParameters = new ECPublicKeyParameters(localECPublicKey.getQ(), localECDomainParameters);

        SM2Engine localSM2Engine = new SM2Engine(new SM3Digest(), SM2Engine.Mode.C1C3C2);
        localSM2Engine.init(true, new ParametersWithRandom(localECPublicKeyParameters, new SecureRandom()));

        byte[] arrayOfByte2;
        arrayOfByte2 = localSM2Engine.processBlock(data, 0, data.length);
        return arrayOfByte2;
    }

    /**
     * 根据privateKey对加密数据encodedata，使用SM2解密
     */
    public static byte[] decrypt(byte[] encodedata, PrivateKey privateKey) throws InvalidCipherTextException {
        SM2Engine localSM2Engine = new SM2Engine(new SM3Digest(), SM2Engine.Mode.C1C3C2);
        BCECPrivateKey sm2PriK = (BCECPrivateKey) privateKey;
        ECParameterSpec localECParameterSpec = sm2PriK.getParameters();
        ECDomainParameters localECDomainParameters = new ECDomainParameters(
                localECParameterSpec.getCurve(), localECParameterSpec.getG(), localECParameterSpec.getN()
        );
        ECPrivateKeyParameters localECPrivateKeyParameters = new ECPrivateKeyParameters(sm2PriK.getD(),
                localECDomainParameters);
        localSM2Engine.init(false, localECPrivateKeyParameters);

        return localSM2Engine.processBlock(encodedata, 0, encodedata.length);

    }

    /**
     * 私钥签名
     */
    public static byte[] signByPrivateKey(byte[] data, PrivateKey privateKey) throws Exception {

        SM2Signer signer = new SM2Signer(StandardDSAEncoding.INSTANCE, new SM3Digest());
        CipherParameters param = new ParametersWithRandom(ECUtil.generatePrivateKeyParameter(privateKey));
        ParametersWithID parametersWithID = new ParametersWithID(
                param, Hex.decodeStrict("31323334353637383132333435363738")
        );

        signer.init(true, parametersWithID);
        signer.update(data, 0, data.length);
        return signer.generateSignature();
    }

    /**
     * 公钥验签
     */
    public static boolean verifyByPublicKey(byte[] data, PublicKey publicKey, byte[] signature) throws Exception {
        SM2Signer signer = new SM2Signer(StandardDSAEncoding.INSTANCE, new SM3Digest());
        ParametersWithID parametersWithID = new ParametersWithID(
                ECUtil.generatePublicKeyParameter(publicKey), Hex.decodeStrict("31323334353637383132333435363738")
        );

        signer.init(false, parametersWithID);
        signer.update(data, 0, data.length);
        return signer.verifySignature(signature);
    }





}
