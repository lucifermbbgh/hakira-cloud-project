package com.hakira.ledger.auth.pojo.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hakira.common.base.BaseDao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.ledger.auth.pojo.dao
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-22  12:27:45
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@TableName("hakira_user_info")
public class HakiraUserInfo extends BaseDao {
    /**
     * @description: 用户ID
     **/
    @TableId(value = "id", type = IdType.AUTO)// 主键自增
    private Long id;
    /**
     * @description: 用户姓氏
     **/
    private String firstname;
    /**
     * @description: 用户姓名
     **/
    private String lastname;
    /**
     * @description: 性别
     **/
    private Integer gender;
    /**
     * @description: 生日
     **/
    private String birthday;
    /**
     * @description: 邮箱
     **/
    private String email;
    /**
     * @description: 联系方式
     **/
    private String telephone;
    /**
     * @description: 国家地区
     **/
    private String country;
    /**
     * @description: 籍贯
     **/
    private String nativePlace;
    /**
     * @description: 居住地
     **/
    private String address;
    /**
     * @description: 居住地邮编
     **/
    private String postcode;
    /**
     * @description: 头像
     **/
    private String avatar;
    /**
     * @description: 创建用户
     **/
    private String createUser;
    /**
     * @description: 创建日期
     **/
    private String createDate;
    /**
     * @description: 创建时间
     **/
    private String createTime;
    /**
     * @description: 更新用户
     **/
    private String updateUser;
    /**
     * @description: 更新日期
     **/
    private String updateDate;
    /**
     * @description: 更新时间
     **/
    private String updateTime;
}
