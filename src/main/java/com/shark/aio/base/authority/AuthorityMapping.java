package com.shark.aio.base.authority;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author lbx
 * @date 2023/4/2 - 21:09
 **/
@Mapper
public interface AuthorityMapping {

    @Select("SELECT * FROM `authority` WHERE `id`=2;")
    AuthorityEntity getCompany();

    @Select("SELECT * FROM `authority` WHERE `id`=3;")
    AuthorityEntity getPark();

    @Select("SELECT * FROM `authority` WHERE `id`=4;")
    AuthorityEntity getFree();

    @Select("SELECT * FROM `authority` WHERE `id`=#{id};")
    AuthorityEntity getAuthority(int id);
    /*
     * 更新
     */
    @Update("UPDATE `authority` SET `authority1`=#{authority1}, `authority2`=#{authority2} ," +
            "`authority3`=#{authority3}, `authority4`=#{authority4},`authority5`=#{authority5},"
            + " `authority6`=#{authority6}, `authority7`=#{authority7} "+ "WHERE `id`=#{id};")
    void updateAuthority(AuthorityEntity object);

}
