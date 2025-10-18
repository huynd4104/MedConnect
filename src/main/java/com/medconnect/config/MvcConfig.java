package com.medconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình để phục vụ các file từ thư mục uploads
        // Đường dẫn URL "/uploads/**" sẽ được map tới thư mục file:src/main/resources/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:src/main/resources/uploads/");
    }
}