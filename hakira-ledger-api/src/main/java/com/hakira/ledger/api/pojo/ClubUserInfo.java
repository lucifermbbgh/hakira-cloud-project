package com.hakira.ledger.api.pojo;

import com.hakira.common.base.BaseDao;
import lombok.Data;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.ledger.api.pojo
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-22  12:27:45
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class ClubUserInfo extends BaseDao {
    private String id;// ID
    private String firstname;// 用户姓氏
    private String lastname;// 用户姓名
    private Byte gender;// 性别
    private String birthdate;// 生日
    private String email;// 邮箱
    private String phone;// 联系方式
    private String country;// 国家地区
    private String nativePlace;// 籍贯
    private String address;// 居住地
    private String postcode;// 居住地邮编
    private String avatar;// 头像
    private String createUser;// 创建用户
    private String createDate;// 创建日期
    private String createTime;// 创建时间
    private String updateUser;// 更新用户
    private String updateDate;// 更新日期
    private String updateTime;// 更新时间
}
