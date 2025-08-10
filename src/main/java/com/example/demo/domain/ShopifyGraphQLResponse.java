package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShopifyGraphQLResponse(Data data) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(ProductVariants productVariants) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductVariants(List<Edge<VariantNode>> edges) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Edge<T>(T node) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VariantNode(
            String id,
            String sku,
            InventoryItem inventoryItem
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InventoryItem(
            String id,
            InventoryLevels inventoryLevels
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InventoryLevels(List<Edge<InventoryLevelNode>> edges) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InventoryLevelNode(Location location) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(String id, String name) {}
}
