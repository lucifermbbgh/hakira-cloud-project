package com.hakira.market.api.common;

import com.hakira.market.api.pojo.ClubUser;
import com.hakira.market.api.pojo.ClubUserInfo;

import java.util.List;
import java.util.Map;

public interface ClubUserInfoService {
    /**
     * @description:注册账户
     * @author: hakiraKafka
     * @date: 2023/11/23 12:07
     * @param: clubUserInfo
     **/
    public void register(ClubUser clubUser);

    /**
     * @description:注销账户
     * @author: hakiraKafka
     * @date: 2023/11/23 16:37
     * @param: id
     **/
    public void unsubscribe(String id);

    /**
     * @description:更新用户信息
     * @author: hakiraKafka
     * @date: 2023/11/23 12:08
     * @param: clubUserInfo
     * @return: com.hakira.club.api.pojo.ClubUserInfo
     **/
    public ClubUserInfo updateInfo(ClubUserInfo clubUserInfo);

    /**
     * @description:更新用户密码
     * @author: hakiraKafka
     * @date: 2023/11/23 16:37
     * @param: clubUser
     * @return: com.hakira.club.api.pojo.ClubUser
     **/
    public ClubUser updatePassword(ClubUser clubUser);

    /**
     * @description:用户查询（单一用户查询）
     * @author: hakiraKafka
     * @date: 2023/11/23 16:35
     * @param: id
     * @return: com.hakira.club.api.pojo.ClubUser
     **/
    public ClubUser getUser(String id);

    /**
     * @description:用户批量查询
     * @author: hakiraKafka
     * @date: 2023/11/23 16:34
     * @param: params
     * @return: java.util.List<com.hakira.club.api.pojo.ClubUser>
     **/
    public List<ClubUser> getUserList(Map<String, Object> params);

    /**
     * @description:用户信息查询（单一用户查询）
     * @author: hakiraKafka
     * @date: 2023/11/23 12:07
     * @param: id
     * @return: com.hakira.club.api.pojo.ClubUserInfo
     **/
    public ClubUserInfo getUserInfo(String id);

    /**
     * @description: 用户信息批量查询
     * @author: hakiraKafka
     * @date: 2023/11/23 16:34
     * @param: params
     * @return: java.util.List<com.hakira.club.api.pojo.ClubUserInfo>
     **/
    public List<ClubUserInfo> getUserInfoList(Map<String, Object> params);
}
