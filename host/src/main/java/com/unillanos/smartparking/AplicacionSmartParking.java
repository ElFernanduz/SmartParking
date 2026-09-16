package com.unillanos.smartparking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AplicacionSmartParking {

    public static void main(String[] args) {
        SpringApplication.run(AplicacionSmartParking.class, args);
    }
}
