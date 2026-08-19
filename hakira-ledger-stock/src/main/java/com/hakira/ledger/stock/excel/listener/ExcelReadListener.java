package com.hakira.ledger.stock.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.hakira.ledger.stock.excel.handler.ExcelWriteHandler;
import com.hakira.ledger.stock.excel.model.StockExcelModel;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: hakira-common
 * @BelongsPackage: com.hakira.common.util.excel
 * @Author: hakiraKafka
 * @CreateTime: 2024-01-28  11:13:52
 * @Description: TODO
 * @Version: 1.0
 */
public class ExcelReadListener extends AnalysisEventListener<StockExcelModel> {
    private String readFilePath;

    private String readWorkBookName;

    private String readSheetName;

    private static List<StockExcelModel> stockExcelModelList = new ArrayList<>();

    private static StockExcelModel tempModel;

    public ExcelReadListener(String readFilePath, String readWorkBookName, String readSheetName) {
        this.readFilePath = readFilePath;
        this.readWorkBookName = readWorkBookName;
        this.readSheetName = readSheetName;
    }

    @Override
    public void invoke(StockExcelModel data, AnalysisContext context) {
        // 读取到下一行数据序号为空，则说明本行数据为换行错行数据，需要将本行单元格数据拼接到上一行每个同列单元格数据的后面
        if (data.getId() == null) {
            tempModel = stockExcelModelList.get(stockExcelModelList.size() - 1);
            tempModel.setId(new StringBuffer(tempModel.getId()).append(trim(data.getId())).toString());
            if (trim(data.getMaterialCode()).startsWith("\'")) {
                String materialCode = data.getMaterialCode().replace("\'", "");
                data.setMaterialCode(materialCode);
                String oriMaterialCode = trim(tempModel.getMaterialCode()).replace("\'", "");
                tempModel.setMaterialCode(oriMaterialCode);
            }
            tempModel.setMaterialCode(new StringBuffer(trim(tempModel.getMaterialCode())).append(trim(data.getMaterialCode())).toString());
            tempModel.setMaterialDescription(new StringBuffer(trim(tempModel.getMaterialDescription())).append(trim(data.getMaterialDescription())).toString());
            tempModel.setPartNumber(new StringBuffer(trim(tempModel.getPartNumber())).append(trim(data.getPartNumber())).toString());
            tempModel.setDrawingNumber(new StringBuffer(trim(tempModel.getDrawingNumber())).append(trim(data.getDrawingNumber())).toString());
            tempModel.setOtherDrawingNumbers(new StringBuffer(trim(tempModel.getOtherDrawingNumbers())).append(trim(data.getOtherDrawingNumbers())).toString());
            tempModel.setSpecificationModel(new StringBuffer(trim(tempModel.getSpecificationModel())).append(trim(data.getSpecificationModel())).toString());
            tempModel.setLatestReplacementNumber(new StringBuffer(trim(tempModel.getLatestReplacementNumber())).append(trim(data.getLatestReplacementNumber())).toString());
            tempModel.setOrderNumber(new StringBuffer(trim(tempModel.getOrderNumber())).append(trim(data.getOrderNumber())).toString());
            tempModel.setReferenceTechnicalRequirements(new StringBuffer(trim(tempModel.getReferenceTechnicalRequirements())).append(trim(data.getReferenceTechnicalRequirements())).toString());
            tempModel.setUnitofMeasurement(new StringBuffer(trim(tempModel.getUnitofMeasurement())).append(trim(data.getUnitofMeasurement())).toString());
            tempModel.setExecutionPrice(new StringBuffer(trim(tempModel.getExecutionPrice())).append(trim(data.getExecutionPrice())).toString());
            stockExcelModelList.remove(stockExcelModelList.size() - 1);
            removeNewLines(tempModel);
            stockExcelModelList.add(tempModel);
            tempModel = null;
        } else {
            removeNewLines(data);
            stockExcelModelList.add(data);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        try {
            String writeFilePath = readFilePath + File.separator + "write";
            ExcelWriteHandler excelWriteHandler = new ExcelWriteHandler(writeFilePath, readWorkBookName, readSheetName, stockExcelModelList);
            excelWriteHandler.writeDataToExcel();
            stockExcelModelList.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String trim(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        } else {
            return str.trim();
        }
    }

    private void removeNewLines(StockExcelModel data){
        // 获取StockExcelModel类的所有字段
        Field[] fields = StockExcelModel.class.getDeclaredFields();

        for (Field field : fields) {
            // 设置访问权限，以便能访问私有字段
            field.setAccessible(true);

            try {
                // 获取字段的值
                Object fieldValue = field.get(data);

                if (fieldValue instanceof String) {
                    // 如果字段值是字符串类型
                    String originalString = (String) fieldValue;

                    // 删除字符串中的连续换行符，并替换为单个换行符（这里假设你希望保留一个换行符）
                    String replacedString = originalString.replaceAll("\\n", "");

                    // 将处理过的字符串重新赋值给字段
                    field.set(data, replacedString);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("无法访问或修改字段值", e);
            }
        }
    }
}
