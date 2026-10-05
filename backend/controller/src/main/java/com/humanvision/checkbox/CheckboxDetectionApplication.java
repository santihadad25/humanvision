package com.humanvision.checkbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CheckboxDetectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(CheckboxDetectionApplication.class, args);
    }
}
