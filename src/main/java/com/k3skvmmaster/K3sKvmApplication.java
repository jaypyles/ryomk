package com.k3skvmmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class K3sKvmApplication {
    public static void main(String[] args) {
        SpringApplication.run(K3sKvmApplication.class, args);
    }
}
