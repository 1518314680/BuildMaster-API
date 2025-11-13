package com.buildmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 启用定时任务
public class BuildMasterApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BuildMasterApiApplication.class, args);
    }
}
