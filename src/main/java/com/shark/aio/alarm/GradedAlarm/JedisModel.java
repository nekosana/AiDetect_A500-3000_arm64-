package com.shark.aio.alarm.GradedAlarm;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

/**
 * @author lbx
 * @date 2023/5/9 - 11:20
 **/
public class JedisModel {

    Jedis jedis = new Jedis("127.0.0.4", 6379);

    @Test
    public void testCheckLogin() {
        System.out.println("lbx" + jedis.ttl("amy"));
        //登录验证时，5分钟内连续输错3次密码，锁住帐号;帐号锁住时间为半个小时，半小时后解封
        checkUser("tom", "123");
    }

    private void checkUser(String loginId, String password) {
        Boolean exit = jedis.exists(loginId);
        if (exit == true && ("true".equals(jedis.get(loginId)))) {
            System.out.println("该帐号已被锁，请确认");
//            throw new CommonBizException("该帐号已被锁，请确认");

        } else if (exit == true && "5".equals(jedis.get(loginId))) {
            //锁帐号
            jedis.set(loginId, "true");
            //设置半个小时的过期时间
            jedis.expire(loginId, 30 * 60);
        } else {
            checkNameAndPassword(loginId, password);
        }
    }

    private void checkNameAndPassword(String loginId, String password) {

        if ("tom".equals(loginId) && "123456".equals(password)) {
            jedis.del(loginId);
            System.out.println("登录成功");

        } else {
            if (jedis.exists(loginId)) {
                jedis.incr(loginId);
            } else {
                jedis.setex(loginId, 24*60*60, "1");
                System.out.println("第"  );
            }
            System.out.println("第" + jedis.get(loginId) + "登录失败");
        }

    }

}