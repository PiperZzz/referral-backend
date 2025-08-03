package org.moyi_tech.usermanagement.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class FileUploadConfig {

    /**
     * Configure multipart resolver for file uploads
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /**
     * Configure multipart file upload settings
     * Max file size: 10MB
     * Max request size: 15MB (including other form data)
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Maximum file size for individual files
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        
        // Maximum size for the entire multipart request
        factory.setMaxRequestSize(DataSize.ofMegabytes(15));
        
        // File size threshold for storing in memory vs disk
        factory.setFileSizeThreshold(DataSize.ofKilobytes(512));
        
        return factory.createMultipartConfig();
    }
}