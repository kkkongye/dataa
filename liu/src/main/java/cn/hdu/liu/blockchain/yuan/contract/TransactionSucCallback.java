package cn.hdu.liu.blockchain.yuan.contract;
import com.thanos.web3j.channel.dto.EthereumResponse;

@FunctionalInterface
public interface TransactionSucCallback {
    void onResponse(EthereumResponse response); // 处理成功结果的方法
}
