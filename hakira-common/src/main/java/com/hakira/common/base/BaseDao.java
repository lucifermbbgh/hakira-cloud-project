package com.hakira.common.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseDao implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer preStatus;
}
