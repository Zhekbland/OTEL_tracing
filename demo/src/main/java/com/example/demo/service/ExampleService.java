package com.example.demo.service;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

@Service
public class ExampleService {

    public void customMethod(Span span) {
        span.setAllAttributes(Attributes.builder().put("key", "value").build());
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void customMethod(Integer delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
