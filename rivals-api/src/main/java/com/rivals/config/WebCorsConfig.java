package com.rivals.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS for local frontend dev:
 *  - Origin: http://localhost:5173 (Vite)
 *  - Methods: GET, POST, OPTIONS
 *  - Headers: *
 *  - Credentials: optional (disabled for now)
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false) // set to true later if you use cookies/session
                .maxAge(3600);
    }
}
