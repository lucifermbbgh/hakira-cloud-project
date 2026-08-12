package com.hakira.common.base;

import com.hakira.common.pojo.common.Result;

import java.util.Map;

public interface ITrade {
    Result excute(Map<String, Object> inputParams);
}