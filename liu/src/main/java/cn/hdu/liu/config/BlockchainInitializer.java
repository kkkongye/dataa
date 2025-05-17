/**package cn.hdu.liu.config;

import cn.hdu.liu.blockchain.yuan.contract.Yuan;
import com.thanos.web3j.crypto.Credentials;
import com.thanos.web3j.protocol.Web3j;
import com.thanos.web3j.protocol.Web3jService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.concurrent.Future;

@Configuration
public class BlockchainInitializer {
    @Value("${blockchain.node.url}")
    private String nodeUrl;

    @Value("${blockchain.admin.privateKey}")
    private String adminPrivateKey;

    @Bean
    public Yuan deployContract() throws Exception {
        // 关键修改：移除强制转换，直接使用 HttpService
        Web3j web3j = Web3j.build((Web3jService) new HttpService(nodeUrl));
        Credentials admin = Credentials.create(adminPrivateKey);

        // 部署合约（仅执行一次）
        Future<Yuan> future = Yuan.deploy(
                web3j,
                admin,
                DefaultGasProvider.GAS_PRICE,
                DefaultGasProvider.GAS_LIMIT,
                BigInteger.ZERO // 初始金额
        );

        Yuan contract = future.get(); // 阻塞等待部署完成
        String contractAddress = contract.getContractAddress();

        // 保存合约地址到配置文件
        saveContractAddress(contractAddress);
        return contract;
    }

    private void saveContractAddress(String address) {
        // 实现：将 address 写入 application.properties 或数据库
    }
}
**/