package com.hakira.ledger.auth.pojo.dao;

import com.hakira.common.base.BaseDao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class HakiraRole extends BaseDao {
    private Long id;// 角色id
    private String roleName;// 角色名
    private Integer status;// 状态
    private String createUser;// 创建用户
    private String createDate;// 创建日期
    private String createTime;// 创建时间
    private String updateUser;// 更新用户
    private String updateDate;// 更新日期
    private String updateTime;// 更新时间
}
