package cn.hdu.liu.dcbds.hdu.Entity;



import java.security.MessageDigest;
import java.util.Date;

public class Block {
    public String hash;
    public String previousHash;
    private BlockData data; // 数据将是一个简单的消息。
    private long timeStamp; // 时间戳
    private int nonce;

    // 构造函数
    public Block(BlockData data, String previousHash ) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash(); // 确保哈希值在其他值被赋值后计算
    }

    // 计算区块的哈希值
    public String calculateHash() {
        String input = previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + data;
        return applySha256(input);
    }

    // 实现SHA256哈希函数
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 应用SHA-256算法
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer(); // 将哈希值转换为十六进制
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 实现工作量证明
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); //创建一个由多个0组成的字符串
        while(!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    public BlockData getData() {
        return data;
    }
}

