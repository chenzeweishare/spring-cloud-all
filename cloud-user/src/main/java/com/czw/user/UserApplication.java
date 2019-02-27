package com.czw.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class UserApplication {

//    @Bean
//    public RedissonClient getRedissonClient(){
//        Config config = new Config();
//        //指定使用单节点部署方式
//        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
//        return Redisson.create(config);
//    }

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
