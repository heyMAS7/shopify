package com.example.demo.application;


import com.example.demo.domain.ShopifyGraphQLResponse;
import com.example.demo.domain.WooCommerceCallbackRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("woo")
@RequiredArgsConstructor
@Slf4j
public class WooCommerceController {


    private final WebClient webClient;

    private final Environment environment;

    private final String baseURl = "https://1v6ir0-d1.myshopify.com";

    @PostMapping(value = "/updateProduct", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateProductCallback(@RequestBody WooCommerceCallbackRequest wooCommerceCallbackRequest){

        ShopifyGraphQLResponse resp = getProductBySku(wooCommerceCallbackRequest.getSku()).block();

        var variantOpt = resp.data()
                .productVariants().edges().stream().findFirst()
                .map(edge -> edge.node());

        if (variantOpt.isEmpty()) {
            log.warn("No product variant found");
            return ResponseEntity.ok().build();
        }

        var gidInventoryItem = variantOpt
                .map(v -> v.inventoryItem())
                .map(item -> item.id())
                .orElse(null);

        if (gidInventoryItem == null) {
            log.warn("Inventory item ID missing for variant");
            return ResponseEntity.ok().build();
        }

        var gidLocation = variantOpt
                .map(v -> v.inventoryItem())
                .map(item -> item.inventoryLevels().edges().stream().findFirst())
                .flatMap(optEdge -> optEdge.map(edge -> edge.node().location().id()))
                .orElse(null);

        if (gidLocation == null) {
            log.warn("Location ID missing for inventory item {}", gidInventoryItem);
            return ResponseEntity.ok().build();
        }

        log.info("Inventory Item: {}, Location: {}", gidInventoryItem, gidLocation);

        String locationId = extractIdFromGid(gidLocation);
        String inventoryItemId = extractIdFromGid(gidInventoryItem);

        Map<String, String> body = Map.of("location_id", locationId,
                "inventory_item_id", inventoryItemId,
                "available", wooCommerceCallbackRequest.getStockQuantity());
        log.info(body.toString());
        JsonNode block = webClient.post()
                .uri(baseURl + "/admin/api/2025-01/inventory_levels/set.json")
                .header("X-Shopify-Access-Token", environment.getProperty("Shopify_Token"))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return ResponseEntity.ok().build();
    }

    public static String extractIdFromGid(String gid) {
        if (gid == null) return null;
        int lastSlash = gid.lastIndexOf("/");
        return lastSlash >= 0 ? gid.substring(lastSlash + 1) : gid;
    }
    public Mono<ShopifyGraphQLResponse> getProductBySku(String sku) {
        String query = """
        {
          productVariants(first: 1, query: "sku:%s") {
            edges {
              node {
                id
                sku
                title
                product {
                  id
                  title
                }
                inventoryItem {
                  id
                  inventoryLevels(first: 10) {
                    edges {
                      node {
                        location {
                          id
                          name
                        }
                        quantities(names: "available") {
                          name
                          quantity
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        """.formatted(sku);

        return webClient.post()
                .uri(baseURl + "/admin/api/2025-01/graphql.json")
                .header("Content-Type", "application/json")
                .header("X-Shopify-Access-Token", environment.getProperty("Shopify_Token"))
                .bodyValue("{\"query\":\"" + query.replace("\"", "\\\"").replace("\n", " ") + "\"}")
                .retrieve()
                .bodyToMono(ShopifyGraphQLResponse.class);
    }

}
