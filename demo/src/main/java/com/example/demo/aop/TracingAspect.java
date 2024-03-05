package com.example.demo.aop;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TracingAspect {

    private final Tracer tracer;

    public TracingAspect(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("innerMethods");
    }

    @Around(value = "execution(* com.example.demo.service.ExampleService.customMethod(Integer)) && args(delayMs)")
    public Object traceAroundCustomMethod(ProceedingJoinPoint joinPoint, Integer delayMs) throws Throwable {
        Span elementSpan = null;
        Object result = null;
        try {
            elementSpan = tracer.spanBuilder("customMethod")
                    .startSpan()
                    .setAllAttributes(
                            Attributes.builder()
                                    .put("type", "delay")
                                    .put("delayMs", delayMs.toString())
                                    .build()

                    );
            result = joinPoint.proceed();
        } catch (Exception e) {
            throw e;
        } finally {
            if (elementSpan != null) {
                elementSpan.end();
            }
        }
        return result;
    }

}
