package com.example.demo;


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
    public void callback(@RequestBody Map<String, Objects> body){
        System.out.println(body);
    }
}
