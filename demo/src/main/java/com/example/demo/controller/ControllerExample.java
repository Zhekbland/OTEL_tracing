package com.example.demo.controller;

import com.example.demo.service.ExampleService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ControllerExample {

    private final RestTemplate restTemplate;
    private final Tracer tracer;
    private final ExampleService service;

    public ControllerExample(OpenTelemetry openTelemetry, RestTemplate restTemplate, ExampleService service) {
        this.restTemplate = restTemplate;
        this.tracer = openTelemetry.getTracer(ControllerExample.class.getName(), "0.1.0");
        this.service = service;
    }

    @GetMapping("/trace")
    public String getFromResource() {
        String response = null;

        response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", String.class);

        return response;
    }

    @GetMapping("/customTrace")
    public String getFromAnother() {
        String response = null;

        Span parentSpan = tracer.spanBuilder("My custom method").startSpan();
        service.customMethod(parentSpan);
        parentSpan.end();

        for (int i = 1; i < 4; i++) {
            response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/" + i, String.class);
        }

        return response;
    }

    @GetMapping("/customAopTrace")
    public String getFromAnotherWithAop() {
        String response = null;

        service.customMethod(250);

        response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", String.class);

        return response;
    }

    @GetMapping("/customTraceWithAnnotation")
    public String getFromAnotherWithAnnotation() {
        String response = null;

        service.customMethodWithAnnotation(300);

        for (int i = 1; i < 4; i++) {
            response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/" + i, String.class);
        }

        return response;
    }

    @GetMapping("/globalTrace")
    public String getFromAnotherService() {
        String response = null;

        service.customMethod(250);

        response = restTemplate.getForObject("http://localhost:8095/microService", String.class);

        return response;
    }

}
