package com.chatokjunior.secureteamapi.testRedis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class RedisTestService {
    private final StringRedisTemplate redis;


    public void demo(int n) {

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()){
            for(int i = 0; i < n; i++) {
               int idx = i;
               executor.submit(() -> {
                   String key = idx + "th key";
                   redis.opsForValue().set(key, "ding", Duration.ofMinutes(1));
                   String value = redis.opsForValue().get(key);
                   Boolean exists = redis.hasKey(key);
                   //    redis.delete(key);
               });
            }
        }
    }
}
