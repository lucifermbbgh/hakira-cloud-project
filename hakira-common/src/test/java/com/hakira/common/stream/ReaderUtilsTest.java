package com.hakira.common.stream;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @BelongsProject: hakira-common
 * @BelongsPackage: com.hakira.common.stream
 * @Author: hakiraKafka
 * @CreateTime: 2024-05-09  14:15:07
 * @Description: TODO
 * @Version: 1.0
 */
public class ReaderUtilsTest {
    @Test
    public void test_1() {
        String filePath = "src/test/resources/test.txt";
        // 读取test.txt文件
        File file = new File(filePath);
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr).append("\n"); // 添加换行符以保持原文件格式
            }
            reader.close();
            System.out.println(sbf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
