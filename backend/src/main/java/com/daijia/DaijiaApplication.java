package com.daijia;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 代驾代办 H5 服务平台启动类
 */
@SpringBootApplication
@MapperScan("com.daijia.**.mapper")
@EnableScheduling
@EnableAsync
public class DaijiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaijiaApplication.class, args);
    }
}
