package com.ssg.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestClient;

@Configuration
public class BeanConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
