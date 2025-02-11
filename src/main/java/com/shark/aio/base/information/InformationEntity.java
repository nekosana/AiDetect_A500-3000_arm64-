package com.shark.aio.base.information;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author lbx
 * @date 2023/4/12 - 17:36
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InformationEntity implements Serializable {
    private Integer id;
    private String company = "请修改";
    private String industry = "请修改";
    private String description = "请修改";
    private String location = "请修改";
    private String telephone = "请修改";
    private static final long serialVersionUID = 42L;

}