package com.hakira.market.stock.excel.handler;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.hakira.market.stock.excel.listener.ExcelReadListener;
import com.hakira.market.stock.excel.model.StockExcelModel;
import lombok.AllArgsConstructor;

/**
 * @BelongsProject: hakira-common
 * @BelongsPackage: com.hakira.common.util.excel
 * @Author: hakiraKafka
 * @CreateTime: 2024-01-28  16:29:19
 * @Description: TODO
 * @Version: 1.0
 */
@AllArgsConstructor
public class ExcelReadHandler {
    private String readFilePath;

    private String readWorkBookName;

    private String readSheetName;

    public void readExcel(String filePath) {
        ExcelReadListener excelReadListener = new ExcelReadListener(readFilePath, readWorkBookName, readSheetName);
        // 注册excel读取监听器进行数据解析，根据指定数据模板，读取指定文件对象
        ExcelReaderBuilder excelReaderBuilder = EasyExcel.read(filePath, StockExcelModel.class, excelReadListener);
        // 自动关闭流
        excelReaderBuilder.autoCloseStream(true);
        // 设置文件类型
        excelReaderBuilder.excelType(ExcelTypeEnum.XLSX);
        // 读取所有数据
        excelReaderBuilder.doReadAll();
    }
}
