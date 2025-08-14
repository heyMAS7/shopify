package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;


@Data
public class WooCommerceCallbackRequest {

    private long id;

    private String name;

    private String slug;

    @JsonAlias("parent_id")
    private String parentId;

    private String sku;
    @JsonAlias("stock_quantity")
    private String stockQuantity;

}
