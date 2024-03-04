package com.example.demo.controller;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@RestController
public class ControllerExample {

    private final RestTemplate restTemplate;
    private final Tracer tracer;

    public ControllerExample(OpenTelemetry openTelemetry, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.tracer = openTelemetry.getTracer(ControllerExample.class.getName(), "0.1.0");
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
        customMethod(parentSpan);
        parentSpan.end();

        for (int i = 0; i < 3; i++) {
            response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", String.class);
        }
        return response;
    }

    private void customMethod(Span span) {
        span.setAllAttributes(Attributes.builder().put("key", "value").build());
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
