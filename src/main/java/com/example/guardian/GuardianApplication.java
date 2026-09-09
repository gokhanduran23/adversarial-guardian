package com.example.guardian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GuardianApplication {
    public static void main(String[] args) {
        SpringApplication.run(GuardianApplication.class, args);
    }
}
