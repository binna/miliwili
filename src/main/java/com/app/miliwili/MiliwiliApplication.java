package com.app.miliwili;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MiliwiliApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiliwiliApplication.class, args);
    }

}
