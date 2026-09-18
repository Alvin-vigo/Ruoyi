package com.ruoyi.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置（若依标准写法）。
 * 仅创建 RedisTemplate<String, Object> Bean，不强制要求本机 Redis 已启动——
 * Lettuce 为懒连接，上下文刷新阶段不会立即建连，应用可正常启动；
 * 只有在真正调用缓存操作时才会连接 Redis。
 */
@Configuration
public class RedisConfig
{
    @Bean
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory)
    {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // key 采用 String 的序列化方式
        template.setKeySerializer(stringSerializer);
        // hash 的 key 也采用 String 的序列化方式
        template.setHashKeySerializer(stringSerializer);
        // value 序列化方式采用 JSON
        template.setValueSerializer(jsonSerializer);
        // hash 的 value 序列化方式采用 JSON
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
