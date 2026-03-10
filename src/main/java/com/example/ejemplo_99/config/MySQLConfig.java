package com.example.ejemplo_99.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.ejemplo_99.repositories.mysql")
@EntityScan(basePackages = "com.example.ejemplo_99.models.mysql")
public class MySQLConfig {
    // La configuración básica se maneja a través de application.properties
}