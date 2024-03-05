package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ControllerExample {

    private final RestTemplate restTemplate;

    public ControllerExample(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/microService")
    public String getFromResource() {
        String response = null;

        response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", String.class);

        return response;
    }

}
