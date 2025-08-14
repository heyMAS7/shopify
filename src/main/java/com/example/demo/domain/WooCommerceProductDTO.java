package com.example.demo.domain;


import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class WooCommerceProductDTO {

    private String id;

    private String name;


    @JsonAlias("parent_id")
    private String parentId;
}
