package com.example.springwebex.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.springwebex.dao.DvdRentalDao;

@Configuration
public class TestDaoMockConfig {

    @Bean
    public DvdRentalDao dvdRentalDao() {
        return Mockito.mock(DvdRentalDao.class);
    }
}
