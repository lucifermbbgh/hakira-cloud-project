package com.hakira.common.util;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

// MD5码工具类代码
public class MD5Util {
    public static final String MD5 = "MD5";
    public static final String HmacMD5 = "HmacMD5";
    public static final String charset = null; // 编码格式；默认null为GBK

    private static MD5Util instance;

    private MD5Util() {
    }

    // 单例
    public static MD5Util getInstance() {
        if (instance == null) {
            synchronized (MD5Util.class) {
                if (instance == null) {
                    instance = new MD5Util();
                }
            }
        }
        return instance;
    }

    /**
     * 使用 MD5 方法加密（无密码）
     */
    public String encode(String res) {
        try {
            MessageDigest md = MessageDigest.getInstance(MD5);
            byte[] resBytes = charset == null ? res.getBytes() : res.getBytes(charset);
            return BASE64Util.getInstance().encode(md.digest(resBytes).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用 MD5 方法加密（可以设密码）
     */
    public String encode(String res, String key) {
        try {
            SecretKey sk = null;
            if (key == null) {
                KeyGenerator kg = KeyGenerator.getInstance(HmacMD5);
                sk = kg.generateKey();
            } else {
                byte[] keyBytes = charset == null ? key.getBytes() : key.getBytes(charset);
                sk = new SecretKeySpec(keyBytes, HmacMD5);
            }
            Mac mac = Mac.getInstance(HmacMD5);
            mac.init(sk);
            byte[] result = mac.doFinal(res.getBytes());
            return BASE64Util.getInstance().encode(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}