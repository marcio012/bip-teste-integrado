package com.example.backend.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.example.ejb")
@EntityScan(basePackages = "com.example.ejb.entity")
public class EjbModuleConfig {
}
