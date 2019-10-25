package com.digitoll.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@ComponentScan({"com.digitoll.erp", "com.digitoll.commons"})
@EnableRetry
public class Erp {
  public static void main(String[] args) {
    SpringApplication.run(Erp.class);
  }
}
