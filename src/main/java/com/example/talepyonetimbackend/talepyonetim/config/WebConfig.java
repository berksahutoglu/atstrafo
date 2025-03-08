package com.example.talepyonetimbackend.talepyonetim.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // Tüm endpoint'ler için
                .allowedOrigins("https://atstalep.com", "http://localhost:3000", "https://atstrafoclient.vercel.app", "https://www.atstalep.com") // Hem production hem development
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "*")
                .exposedHeaders("Authorization")
                .allowCredentials(true) // Domain belirtildiğinde true olmalı
                .maxAge(3600);
    }
}
