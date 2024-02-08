package com.example.springwebex.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.TransactionManager;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class H2DbConfig {
    @Bean
    public DataSource hikariDataSource() {
        log.info("register Bean = hikariDataSource");
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName("org.h2.Driver");
        hikariDataSource.setJdbcUrl("jdbc:h2:mem:test");
        hikariDataSource.setUsername("sa");
        hikariDataSource.setPassword("");
        return hikariDataSource;
    }

    @Bean
    public TransactionManager transactionManager() {
        log.info("register Bean = transactionManager");
        return new JdbcTransactionManager(hikariDataSource());
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        log.info("register Bean = jdbcTemplate ");
        return new JdbcTemplate(hikariDataSource());
    }

}
