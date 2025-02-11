package com.shark.aio.alarm.GradedAlarm.neww;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisListenerConfig {
    @Autowired
    RedisListenerErrorHandle redisListenerErrorHandle;
    /**
     * Redis 消息监听器容器.
     *
     * @param redisConnectionFactory the redis connection factory
     * @return the redis message listener container
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.setErrorHandler(redisListenerErrorHandle);
        return redisMessageListenerContainer;
    }


    /**
     * Redis Key失效监听器注册为Bean.
     *
     * @param redisMessageListenerContainer the redis message listener container
     * @return the redis event message listener
     */
    @Bean
    public RedisKeyExpirationListener redisEventMessageListener(RedisMessageListenerContainer redisMessageListenerContainer){
        return new RedisKeyExpirationListener(redisMessageListenerContainer);
    }

}
