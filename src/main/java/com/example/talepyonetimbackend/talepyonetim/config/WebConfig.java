package com.example.talepyonetimbackend.talepyonetim.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // Tüm endpoint'ler için
                .allowedOrigins("*") // Geliştirme aşaması için. Daha sonra frontend domainini ekleyin
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "*")
                .exposedHeaders("Authorization")
                .allowCredentials(false) // * origin kullanıldığında false olmalı
                .maxAge(3600);
    }
}
