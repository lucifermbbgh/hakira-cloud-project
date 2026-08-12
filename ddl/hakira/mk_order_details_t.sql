DROP TABLE IF EXISTS `mk_order_details_t`;
CREATE TABLE `mk_order_details_t`
(
    `DT_PT`             SMALLINT UNSIGNED NOT NULL COMMENT '日期分区键',
    `SERIAL_ID`         VARCHAR(30)       NOT NULL COMMENT '全局唯一序号',
    `ORDER_ID`          VARCHAR(30)       NOT NULL COMMENT '订单编号',
    `AMOUNT`            DECIMAL(16, 0)    NOT NULL COMMENT '交易金额',
    `CURRENCY`          CHAR(3)           NOT NULL COMMENT '交易币种号',
    `PRODUCT_CODE`      CHAR(10)          NOT NULL COMMENT '商品编号',
    `UNIT_PRICE`        DECIMAL(16, 2)    NOT NULL COMMENT '商品单价',
    `QUANTITY`          INT UNSIGNED      NOT NULL COMMENT '商品数量',
    `ORDER_TIMESTAMP`   CHAR(23)          NOT NULL COMMENT '订单创建时间戳',
    `STATUS`            TINYINT UNSIGNED  NOT NULL COMMENT '订单状态',
    `SUMMARY`           VARCHAR(100)   DEFAULT NULL COMMENT '摘要',
    `CREATED_DATA_DATE` CHAR(10)       DEFAULT '1900-01-01' COMMENT '数据创建日期',
    `CREATED_DATA_TIME` CHAR(8)        DEFAULT '00:00:00' COMMENT '数据创建时间',
    `LAST_UPDATE_DATE`  CHAR(10)       DEFAULT '1900-01-01' COMMENT '数据最后更新日期',
    `LAST_UPDATE_TIME`  CHAR(8)        DEFAULT '00:00:00' COMMENT '数据最后更新时间',
    `BAK1`              VARCHAR(255)   DEFAULT NULL COMMENT '备用字段1',
    `BAK2`              INT            DEFAULT NULL COMMENT '备用字段2',
    `BAK3`              DECIMAL(16, 2) DEFAULT NULL COMMENT '备用字段3'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC COMMENT ='订单明细表临时表';
