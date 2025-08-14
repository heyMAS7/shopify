package com.example.demo.domain;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("")
public class ShopifyProviderProperties {
    private String baseURL;

}
