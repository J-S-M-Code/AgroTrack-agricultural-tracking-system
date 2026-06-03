package com.agrotrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AgroTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgroTrackApplication.class, args);
    }

}
