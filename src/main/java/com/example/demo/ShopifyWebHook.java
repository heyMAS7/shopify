package com.example.demo;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
public class ShopifyWebHook {


    @GetMapping
    public String healthCheck(){
        return "working";
    }


    @PostMapping("/shop")
    public ResponseEntity<?> callback(@RequestBody JsonNode jsonNode){
        System.out.println("Webhook received: " + jsonNode.toPrettyString());
        return ResponseEntity.ok(jsonNode.toPrettyString());
    }
}
