package com.hakira.club.article.algorithm;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.club.article.algorithm
 * @Author: hakiraKafka
 * @CreateTime: 2025-02-01  21:17:02
 * @Description: TODO
 * @Version: 1.0
 */
public class D_sumTwoNum {
    /**
     * @description:给定一个整数数组 nums 和一个整数目标值 target，
     * 请你在该数组中找出 和为目标值 target  的那 两个 整数，并返回它们的数组下标。
     * 你可以假设每种输入只会对应一个答案，并且你不能使用两次相同的元素。
     * 你可以按任意顺序返回答案。
     * @author: hakiraKafka
     * @date: 2025/2/1 21:17
     * @param: null
     * @return: null
     **/
    public static int[] twoSum(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = 0; j < nums.length; j++) {
                int result = nums[i] + nums[j];
                if (result == target && i != j) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        int[] nums = {3, 2, 4};
        int target = 6;
        int[] ints = twoSum(nums, target);
        System.out.println(JSONObject.toJSONString(ints));
    }
}
