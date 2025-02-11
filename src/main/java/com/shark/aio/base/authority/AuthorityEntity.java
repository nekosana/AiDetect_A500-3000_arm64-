package com.shark.aio.base.authority;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author lbx
 * @date 2023/4/2 - 19:42
 **/
@Data
@ToString
public class AuthorityEntity{
    private int id;
    private String authority1 = "off";
    private String authority2 = "off";
    private String authority3 = "off";
    private String authority4 = "off";
    private String authority5 = "off";
    private String authority6 = "off";
    private String authority7 = "off";
    private String authority8 = "off";
}
