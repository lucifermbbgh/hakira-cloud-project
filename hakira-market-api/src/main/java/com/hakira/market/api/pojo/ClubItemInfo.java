package com.hakira.market.api.pojo;

import com.hakira.common.base.BaseDao;
import lombok.Data;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.club.api.pojo
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-22  12:27:59
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class ClubItemInfo extends BaseDao {
    private String id;// 功能项id
    private String itemName;// 功能项名
    private Integer status;// 状态
    private String createUser;// 创建用户
    private String createDate;// 创建日期
    private String createTime;// 创建时间
    private String updateUser;// 更新用户
    private String updateDate;// 更新日期
    private String updateTime;// 更新时间
}
