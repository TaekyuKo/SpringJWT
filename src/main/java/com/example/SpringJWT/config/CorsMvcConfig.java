package com.example.SpringJWT.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {
    //Controller단의 CORS 해결을 위한 Class
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**") //모든 Controller의 경로에 대해
                .allowedOrigins("http://localhost:3000");  //프론트엔드쪽 요청주소
    }
}
