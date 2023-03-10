package org.pepdev.birdnest;

import lombok.extern.slf4j.Slf4j;
import org.pepdev.birdnest.service.WebSocketRedisListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.util.Arrays;

@SpringBootApplication
@EnableScheduling
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
@Slf4j
public class BirdnestApplication {

    @Value("${birdnest.redis.url}")
    private String REDIS_URL;

    @Value("${birdnest.redis.port}")
    private int REDIS_PORT;

    public static void main(String[] args) {
        SpringApplication.run(BirdnestApplication.class, args);
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(REDIS_URL, REDIS_PORT);
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
    @Bean
    RedisMessageListenerContainer keyExpirationListenerContainer(JedisConnectionFactory connectionFactory, WebSocketRedisListener webSocketRedisListener) {

        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory);

        listenerContainer.addMessageListener(webSocketRedisListener, Arrays.asList(new PatternTopic("__key*__:expired"), new PatternTopic("__keyevent@*__:hset")));
        listenerContainer.setErrorHandler((e)-> log.error("There was an error during the expiration listener : ", e));

        return listenerContainer;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }

}
