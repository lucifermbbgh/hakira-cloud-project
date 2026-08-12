package com.hakira.market.stock.excel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @BelongsProject: hakira-common
 * @BelongsPackage: com.hakira.common.pojo.excel
 * @Author: hakiraKafka
 * @CreateTime: 2024-01-28  11:17:39
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockExcelModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * @description:序号
     **/
    @ExcelProperty(value = "序号", index = 0)
    private String id;
    /**
     * @description:物料编码
     **/
    @ExcelProperty(value = "物料编码", index = 1)
    private String materialCode;
    /**
     * @description:物料描述
     **/
    @ExcelProperty(value = "物料描述", index = 2)
    private String materialDescription;
    /**
     * @description:件号
     **/
    @ExcelProperty(value = "件号", index = 3)
    private String partNumber;
    /**
     * @description:图号
     **/
    @ExcelProperty(value = "图号", index = 4)
    private String drawingNumber;
    /**
     * @description:其他图件号
     **/
    @ExcelProperty(value = "其他图件号", index = 5)
    private String otherDrawingNumbers;
    /**
     * @description:规格型号
     **/
    @ExcelProperty(value = "规格型号", index = 6)
    private String specificationModel;
    /**
     * @description:最新替代号
     **/
    @ExcelProperty(value = "最新替代号", index = 7)
    private String latestReplacementNumber;
    /**
     * @description:订货号
     **/
    @ExcelProperty(value = "订货号", index = 8)
    private String orderNumber;
    /**
     * @description:参考技术要求
     **/
    @ExcelProperty(value = "参考技术要求", index = 9)
    private String referenceTechnicalRequirements;
    /**
     * @description:计量单位
     **/
    @ExcelProperty(value = "计量单位", index = 10)
    private String unitofMeasurement;
    /**
     * @description:执行单价（不含税，元）
     **/
    @ExcelProperty(value = "执行单价", index = 11)
    private String executionPrice;
}
