package com.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringBootBloggingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootBloggingPlatformApplication.class, args);
    }

}
