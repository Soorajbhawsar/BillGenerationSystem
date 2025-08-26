package com.anant.vastralok2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        // If DATABASE_URL is provided (Render environment), parse it
        if (databaseUrl != null && !databaseUrl.isEmpty() && databaseUrl.startsWith("postgresql://")) {
            return parseRenderDatabaseUrl(databaseUrl);
        }
        
        // Otherwise, let Spring Boot use application.properties configuration
        return DataSourceBuilder.create().build();
    }

    private DataSource parseRenderDatabaseUrl(String databaseUrl) {
        try {
            // Format: postgresql://username:password@host:port/database
            String cleanUri = databaseUrl.substring("postgresql://".length());
            
            String[] parts = cleanUri.split("@");
            String[] userPass = parts[0].split(":");
            String[] hostPortDb = parts[1].split("/");
            String[] hostPort = hostPortDb[0].split(":");
            
            String username = userPass[0];
            String password = userPass[1];
            String host = hostPort[0];
            String port = hostPort.length > 1 ? hostPort[1] : "5432";
            String database = hostPortDb[1];
            
            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
            
            return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse DATABASE_URL: " + databaseUrl, e);
        }
    }
}
