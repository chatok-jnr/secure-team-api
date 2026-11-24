package com.chatokjunior.secureteamapi.testRedis;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/redis")
@RequiredArgsConstructor
public class RedisTestController {

    private final RedisTestService redisTestService;

    @GetMapping("/{cnt}")
    public void test(String key,
                     @PathVariable int cnt) {
        redisTestService.demo(cnt);
    }
}
