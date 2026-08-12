package com.hakira.club.article.pojo.dao;

import com.hakira.common.base.BaseDao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.club.article.pojo.dao
 * @Author: hakiraKafka
 * @CreateTime: 2023-12-05  23:55:55
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubArticleInfo extends BaseDao {
    private String id;
    private String author;
    private Integer status;
    private String articleTitle;
    private String articleContent;
    private String articleImg;
    private String articleDate;
    private String articleType;
    private String articleTag;
    private String articleComment;
    private String articleView;
    private String articleLike;
    private String createUser;// 创建用户
    private String createDate;// 创建日期
    private String createTime;// 创建时间
    private String updateUser;// 更新用户
    private String updateDate;// 更新日期
    private String updateTime;// 更新时间
}
