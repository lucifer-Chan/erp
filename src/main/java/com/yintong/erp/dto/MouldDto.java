package com.yintong.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author lucifer.chan
 * @create 2018-09-08 下午11:33
 * 模具Dto
 **/

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MouldDto {

    private Long id;

    //总数
    private Double num;
}
