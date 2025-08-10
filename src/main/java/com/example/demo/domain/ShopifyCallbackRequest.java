package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;


@Data
public class ShopifyCallbackRequest {

    private String id;
    private String title;

    private String vendor;

    private List<ProductVariant> variants;



    @Data
    public static class ProductVariant {

        private String sku;

        @JsonProperty("inventory_item_id")
        private String inventoryItemId;

        @JsonProperty("updated_at")
        private ZonedDateTime updatedAt;

        @JsonProperty("product_id")
        private long productId;


    }
}



