package com.shark.aio.alarm.GradedAlarm;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author lbx
 * @date 2023/6/25 - 17:21
 **/

@ConfigurationProperties(prefix = "spring.redis")
@Data
//public class JedisConnectionFactory {
//
//    private final static JedisPool jedisPool;
//
//    static {
//        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//
//        jedisPoolConfig.setMaxTotal(100);//最大空闲连接
//        jedisPoolConfig.setMaxIdle(20);//最小空闲连接
//        jedisPoolConfig.setMinIdle(5);//设置最长等待时间， ms
//        jedisPoolConfig.setMaxWaitMillis(200);
//        jedisPool = new JedisPool(jedisPoolConfig, "192.168.0.122", 6379,5000);
//    }
//
//    //获取 Jedis 对象
//    public static Jedis getJedis() {
//
//        Jedis jedis = null;
//        if (jedisPool != null) {
//            //从连接池中获取Jedis对象
//            jedis = jedisPool.getResource();
//        }
//        //返回对象
//        return jedis;
//    }
//}
public class JedisConnectionFactory implements InitializingBean {
    private JedisPool jedisPool;

    private String host;

    private Integer port;


    //获取 Jedis 对象
    public Jedis getJedis() {

        Jedis jedis = null;
        if (jedisPool != null) {
            //从连接池中获取Jedis对象
            jedis = jedisPool.getResource();
        }
        //返回对象
        return jedis;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        jedisPoolConfig.setMaxTotal(100);//最大空闲连接
        jedisPoolConfig.setMaxIdle(20);//最小空闲连接
        jedisPoolConfig.setMinIdle(5);//设置最长等待时间， ms
        jedisPoolConfig.setMaxWaitMillis(200);
        this.jedisPool = new JedisPool(jedisPoolConfig, host, port,5000);
    }
}