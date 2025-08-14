package com.example.demo.application;


import com.example.demo.domain.ShopifyCallbackRequest;
import com.example.demo.domain.WooCommerceProductDTO;
import com.example.demo.domain.WooCommerceProviderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("shopify")
@RequiredArgsConstructor
@Slf4j
public class ShopifyController {

    private final WebClient webClient;

    @GetMapping
    public String healthCheck(){
        return "working 2";
    }

    private final WooCommerceProviderProperties wooCommerceProviderProperties;

    private final Environment environment;
    @PostMapping("/shop")
    public ResponseEntity<?> callback(@RequestBody ShopifyCallbackRequest shopifyCallbackRequest){

        ShopifyCallbackRequest.ProductVariant latest = shopifyCallbackRequest.getVariants().stream()
                .max(Comparator.comparing(ShopifyCallbackRequest.ProductVariant::getUpdatedAt))
                .orElse(null);

        assert latest != null;

        log.info("ProductVariant" + latest);

        List<WooCommerceProductDTO> productDTO = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(wooCommerceProviderProperties.getBaseURL())
                        .path("/wp-json/wc/v3/products")
                        .queryParam("sku", latest.getSku())
                        .build())
                .headers(httpHeaders -> httpHeaders.setBasicAuth(environment.getProperty("WOO_CONSUMER_KEY"), environment.getProperty("WOO_CONSUMER_SECRET")))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<WooCommerceProductDTO>>() {})
                .block();

        assert productDTO != null;
        log.info(productDTO.toString());

        if (!productDTO.isEmpty()){

            String productId = productDTO.get(0).getId();

            webClient.put()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(wooCommerceProviderProperties.getBaseURL())
                            .path("/wp-json/wc/v3/products/{id}")
                            .build(productId))
                    .headers(httpHeaders -> httpHeaders.setBasicAuth(environment.getProperty("WOO_CONSUMER_KEY"), environment.getProperty("WOO_CONSUMER_SECRET")))
                    .bodyValue(Map.of("stock_quantity", latest.getInventoryQuantity()))
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> {
                        if (response.getStatusCode().is2xxSuccessful()) {
                            System.out.println("✅ Update successful!");
                        } else {
                            System.out.println("❌ Failed with status: " + response.getStatusCode());
                        }
                        return response.getStatusCode();
                    })
                    .block();
        }


        return ResponseEntity.ok().build();
    }

}
