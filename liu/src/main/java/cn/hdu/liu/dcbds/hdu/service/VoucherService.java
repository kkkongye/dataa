package cn.hdu.liu.dcbds.hdu.service;

import cn.hutool.json.JSONObject;
import cn.hdu.liu.dcbds.hdu.Entity.Voucher;
import cn.hdu.liu.dcbds.hdu.bswabe.BswabePub;

import java.security.PrivateKey;

public interface VoucherService {
    Voucher getVoucher(BswabePub pub, String id, String Did, JSONObject rps, JSONObject hrp, JSONObject grp, PrivateKey sk) throws Exception;
}
