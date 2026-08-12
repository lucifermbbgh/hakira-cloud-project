//package com.hakira.gate.config;
//
//import feign.Logger;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * @BelongsProject: hakira
// * @BelongsPackage: com.hakira.gate.order.config
// * @Author: hakiraKafka
// * @CreateTime: 2023-11-29  22:13:33
// * @Description: TODO
// * @Version: 1.0
// */
//@Configuration
//public class OpenFeignLoggerConfig {
//    /**
//     * @description:feign的日志级别设置为最大级别
//     * @description:- NONE：默认的，不显示任何日志；
//     * @description:- BASIC：仅记录请求方法、URL、响应状态码及执行时间；
//     * @description:- HEADERS：除了 BASIC 中定义的信息之外，还有请求和响应的头信息；
//     * @description:- FULL：除了 HEADERS 中定义的信息之外，还有请求和响应的正文及元数据。
//     * @author: hakiraKafka
//     * @date: 2023/11/29 22:15
//     * @return: feign.Logger.Level
//     **/
//    @Bean
//    Logger.Level openFeignLoggerLever() {
//        return Logger.Level.FULL;
//    }
//}
