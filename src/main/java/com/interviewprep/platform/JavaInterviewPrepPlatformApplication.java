package com.interviewprep.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
@EnableAsync
public class JavaInterviewPrepPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(JavaInterviewPrepPlatformApplication.class, args);
    }
}
