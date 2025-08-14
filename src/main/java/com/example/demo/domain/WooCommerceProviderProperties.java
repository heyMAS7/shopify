package com.example.demo.domain;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("woocommerce")
@Setter
@Getter
public class WooCommerceProviderProperties {
    private String baseURL = "el-lbs.com";

    private String consumerKey;

    private String consumerSecret;

}
