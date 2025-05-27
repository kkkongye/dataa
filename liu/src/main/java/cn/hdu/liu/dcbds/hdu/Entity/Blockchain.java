package cn.hdu.liu.dcbds.hdu.Entity;



import java.util.ArrayList;

public class Blockchain {
    public ArrayList<Block> blockchain;
    public int difficulty;

    public Blockchain(int difficulty) {
        this.blockchain = new ArrayList<>();
        this.difficulty = difficulty;
        // 添加创世区块
        blockchain.add(createGenesisBlock());
    }

    private Block createGenesisBlock() {
        // 创建默认的 BlockData（示例参数需根据实际业务调整）
        byte[] defaultPK = new byte[0]; // 示例公钥
        BlockData genesisData = new BlockData(
                defaultPK,
                defaultPK,
                "genesis_cid",
                new byte[0],
                new byte[0],
                0
        );
        return new Block(genesisData, "0");
    }

    public void addBlock(Block newBlock) {
        newBlock.previousHash = blockchain.get(blockchain.size() - 1).hash;
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

    public boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;

        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);

            // 检查当前区块的哈希值是否正确
            if(!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");
                return false;
            }

            // 检查前一个区块的哈希值是否等于当前区块存储的前一个哈希值
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
        }
        return true;
    }

    public Block getLastBlock(String cid){
        for (int i = blockchain.size()-1; i >= 0; i--) {
            if (blockchain.get(i).getData().getCid().equals(cid)){
                return blockchain.get(i);
            }
        }
        return null;
    }
}
