package cn.hdu.liu.service.impl;

import cn.hdu.liu.utils.Sm4Utils;
import cn.hutool.json.JSONObject;
import cn.hdu.liu.dcbds.hdu.Entity.DataCapsule;
import cn.hdu.liu.dcbds.hdu.Entity.Sm2KeyEntity;
import cn.hdu.liu.dcbds.hdu.Entity.Voucher;
import cn.hdu.liu.dcbds.hdu.Entity.WaterPrint;
import cn.hdu.liu.dcbds.hdu.Utils.Common;
import cn.hdu.liu.dcbds.hdu.Utils.Sm2Utils;
import cn.hdu.liu.dcbds.hdu.Utils.Sm3Utils;
import cn.hdu.liu.dcbds.hdu.bswabe.*;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import cn.hdu.liu.obj.BswabePub;
import org.springframework.stereotype.Service;


public class DPServiceImpl {

}