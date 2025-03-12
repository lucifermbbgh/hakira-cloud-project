DROP table if exists `elysia`.`market_order_info`;
Create table `elysia`.`market_order_info`(
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` varchar(64) NOT NULL COMMENT '订单id',
    `order_type` varchar(32) NOT NULL COMMENT '订单类型',
    `order_status` varchar(32) NOT NULL COMMENT '订单状态',
    `order_price` decimal(10,2) NOT NULL COMMENT '订单价格',
    `order_count` int(11) NOT NULL COMMENT '订单数量',
    `order_total_price` decimal(10,2) NOT NULL COMMENT '订单总价',
    `order_create_time` datetime NOT NULL COMMENT '订单创建时间',
    `order_update_time` datetime NOT NULL COMMENT '订单更新时间',
    `order_create_user` varchar(32) NOT NULL COMMENT '订单创建用户',
    `order_update_user` varchar(32) NOT NULL COMMENT '订单更新用户',
    `order_remark` varchar(128) NOT NULL COMMENT '订单备注',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='订单信息表';

-- 生成用户订单表MYSQL DDL

