package com.digitoll.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@ComponentScan({"com.digitoll.integration", "com.digitoll.commons"})
@EnableRetry
public class Integration {
    public static void main(String[] args) {
        SpringApplication.run(Integration.class);
    }
}
