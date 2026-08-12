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
public class ClubUser extends BaseDao {
    private String id;// ID
    private String username;// 用户名
    private String password;// 用户密码
    private Byte status;// 用户账号状态
    private String roleId;// 用户角色ID
    private String registrationDate;// 用户注册日期
    private String lastLoginDate;// 最后登录日期
    private String createUser;// 创建用户
    private String createDate;// 创建日期
    private String createTime;// 创建时间
    private String updateUser;// 更新用户
    private String updateDate;// 更新日期
    private String updateTime;// 更新时间
}
