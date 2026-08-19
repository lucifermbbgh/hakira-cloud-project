package com.hakira.ledger.stock.excel.handler;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.hakira.ledger.stock.excel.model.StockExcelModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @BelongsProject: hakira-common
 * @BelongsPackage: com.hakira.common.util.excel
 * @Author: hakiraKafka
 * @CreateTime: 2024-01-28  11:13:52
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
public class ExcelWriteHandler {
    private String writeFilePath;

    private String writeWorkBookName;

    private String writeSheetName;

    private List<StockExcelModel> stockExcelModelList;

    public ExcelWriteHandler(String writeFilePath, String writeWorkBookName, String writeSheetName, List<StockExcelModel> stockExcelModelList) {
        this.writeFilePath = writeFilePath;
        this.writeWorkBookName = writeWorkBookName;
        this.writeSheetName = writeSheetName;
        this.stockExcelModelList = stockExcelModelList;
    }

    public void writeDataToExcel() throws Exception {
        // 获取文件路径
        String filePath = writeFilePath + File.separator + writeWorkBookName + " - 副本.xlsx";
        // 创建PATH对象
        Path excelFilePath = Paths.get(filePath);
        // 创建文件夹
        try {
            Files.createDirectories(excelFilePath.getParent());
        } catch (IOException e) {
            log.error("Failed to create directory: " + excelFilePath.getParent() + " " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
        // 创建文件对象
        File file = excelFilePath.toFile();
        // 开始写入数据
        try {
            EasyExcel.write(file)
                    .excelType(ExcelTypeEnum.XLSX) // 指定Excel类型，默认为.xlsx
                    .head(StockExcelModel.class)
                    .sheet(writeSheetName)
                    .doWrite(stockExcelModelList);
        } catch (Exception e) {
            String errorMsg = "Failed to write data into Excel file: " + ExceptionUtils.getStackTrace(e);
            log.error(errorMsg);
            throw e;
        }
    }
}
