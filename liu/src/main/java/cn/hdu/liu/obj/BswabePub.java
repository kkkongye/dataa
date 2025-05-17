package cn.hdu.liu.obj;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import java.io.InputStream;
import java.util.Properties;

public class BswabePub {
	public String pairingDesc;
	public Pairing p;                // 双线性配对对象
	public Element g;                // G1 群生成元
	public Element h;                // G1 群元素
	public Element f;                // G1 群元素
	public Element gp;               // G2 群生成元
	public Element g_hat_alpha;      // GT 群元素

	// 默认构造函数，从类路径加载参数文件
	public BswabePub() {
		this("params.properties");  // 默认加载类路径下的 params.properties
	}

	// 自定义构造函数，指定参数文件路径
	public BswabePub(String paramPath) {
		try {
			// 加载配对参数
			this.p = PairingFactory.getPairing(paramPath);

			// 初始化群元素
			this.g = p.getG1().newRandomElement().getImmutable();  // G1 生成元
			this.h = p.getG1().newRandomElement().getImmutable();
			this.f = p.getG1().newRandomElement().getImmutable();
			this.gp = p.getG2().newRandomElement().getImmutable(); // G2 生成元

			// 计算 g_hat_alpha = e(g, gp)^α，其中 α 是随机数
			Element alpha = p.getZr().newRandomElement().getImmutable();
			this.g_hat_alpha = p.pairing(g, gp).powZn(alpha).getImmutable();

		} catch (Exception e) {
			throw new RuntimeException("初始化 BswabePub 失败: " + e.getMessage(), e);
		}
	}
}