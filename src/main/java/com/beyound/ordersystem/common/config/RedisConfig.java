package com.beyound.ordersystem.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
//    redis 빈 객체를 하나 만들고 사용하는 쪽에서 주입받아서 사용

    @Value("${spring.redis.host1}")
    public String host;

    @Value("${spring.redis.port1}")
    public int port;

    //    연결빈 객체 ( 레디스의 연결 정보 )
//    Qqualifier : 같은 Bean객체가 여러개 있을 경우 Bean객체를 구분하기 위한 어노테이션
    @Bean
    @Qualifier("rtInventory")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();

        configuration.setHostName(host);

        configuration.setPort(port);

        configuration.setDatabase(0);

        return new LettuceConnectionFactory();
    }

    //    템플릿빈 객체 (자료구조 타입 설계)
//    레디스를 서버에서 사용하고자 할 때 이 빈을 주입 받아서 사용해야 함
    @Bean
    @Qualifier("rtInventory")
//    모든 template중에 무조건 redisTemplate라는 메서드 명이 반드시 1개는 있어여함
//   bean객체 생성 시, bean객체 간에 DI(의존성 주입)는 "메서드 파라미터 주입"이 가능
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory")RedisConnectionFactory redisConnectionFactory) { // 해당 redisConnectionFactory는 위에 선언한 Bean 객체다

        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
//        redis에 키를 저장 하는데 string으로 만들어서 넣는다
        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redis에 밸류르 저장 하는데 string으로 만들어서 넣는다
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        return redisTemplate;
    }


    @Bean
    @Qualifier("stockInventory")
    public RedisConnectionFactory stockConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();

        configuration.setHostName(host);

        configuration.setPort(port);

        configuration.setDatabase(1);

        return new LettuceConnectionFactory();
    }

    @Bean
    @Qualifier("stockInventory")
    public RedisTemplate<String, String> stockRedisTemplate(@Qualifier("stockInventory")RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        return redisTemplate;
    }

}
