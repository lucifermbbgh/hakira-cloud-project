package com.hakira.common.util;

import java.util.Date;
import java.util.Random;

public class IdGeneratorUtil {
    public static String getId() {
        // 创建ID-20位
        StringBuffer id = new StringBuffer("");

        // yyyyMMddHHmmss-14位日期时间戳
        String detailLSHFormat = DateUtils.detailLSHFormat(new Date());

        // 6位随机数
        String random = getRandom(6);

        // ID字段拼接获取20位字符串
        return id.append(detailLSHFormat).append(random).toString();
    }

    /**
     * 获取指定长度随机数
     *
     * @param len
     * @return
     */
    public static String getRandom(int len) {
        Random r = new Random();
        int bitNum = 1;
        for (int i = 0; i < len; i++) {
            bitNum = bitNum * 10;
        }
        long num = Math.abs(r.nextLong() % (bitNum));
        String s = String.valueOf(num);
        for (int i = len - s.length(); i > 0; i--) {
            s = "0" + s;
        }
        if (s.length() > len) {
            s = s.substring(0, len);
        }
        return s;
    }
}
