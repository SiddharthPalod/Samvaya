package com.eventverse.mediaservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for serving static media files (acts like a CDN).
 * Serves files from the storage path with long cache headers.
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${media.storage.path}")
    private String storagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Normalize path (handle relative paths)
        String normalizedPath = storagePath;
        if (!normalizedPath.startsWith("/") && !normalizedPath.startsWith("file:")) {
            // Relative path - make it absolute
            try {
                java.nio.file.Path path = java.nio.file.Paths.get(storagePath).toAbsolutePath().normalize();
                normalizedPath = path.toString();
            } catch (Exception e) {
                // Fallback to original path
            }
        }
        
        // Ensure path ends with separator for file: protocol
        if (!normalizedPath.endsWith("/") && !normalizedPath.endsWith("\\")) {
            normalizedPath += java.io.File.separator;
        }
        
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:" + normalizedPath)
                .setCachePeriod(31536000); // 1 year cache
    }
}

