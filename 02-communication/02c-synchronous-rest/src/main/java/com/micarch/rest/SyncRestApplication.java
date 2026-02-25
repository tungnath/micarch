package com.micarch.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SyncRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncRestApplication.class, args);
    }
}
