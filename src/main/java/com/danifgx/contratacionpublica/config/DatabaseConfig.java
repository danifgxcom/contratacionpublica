package com.danifgx.contratacionpublica.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Configuration class for database-related beans.
 */
@Configuration
@Slf4j
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * Creates a DataSource with retry capabilities.
     * This will make the application wait for the database to be available before trying to connect to it.
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Configuring DataSource with URL: {}, Username: {}, Driver: {}", url, username, driverClassName);

        try {
            HikariDataSource dataSource = DataSourceBuilder.create()
                    .type(HikariDataSource.class)
                    .url(url)
                    .username(username)
                    .password(password)
                    .driverClassName(driverClassName)
                    .build();

            // Configure connection pool
            dataSource.setConnectionTimeout(60000); // 60 seconds
            dataSource.setMaximumPoolSize(5);
            dataSource.setMinimumIdle(2);
            dataSource.setInitializationFailTimeout(60000); // 60 seconds

            log.info("DataSource configured successfully with connection timeout: {} ms", dataSource.getConnectionTimeout());
            return dataSource;
        } catch (Exception e) {
            log.error("Error configuring DataSource", e);
            throw e;
        }
    }

    /**
     * Creates a transaction manager for the application.
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

}
