package com.example.demo.aop;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
public class TracingAspect {

    private final Tracer tracer;

    public TracingAspect(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("innerMethods");
    }

    @Pointcut("execution(* com.example.demo.controller.ControllerExample.getFromAnother(..))" +
            " || execution(* com.example.demo.controller.ControllerExample.getFromAnotherWithAop(..))")
    public void pointcut() {

    }

    @Around(value = "pointcut()")
    public Object traceAroundGetFromAnother(ProceedingJoinPoint joinPoint) throws Throwable {
        Span elementSpan = null;
        Scope scope = null;
        Object result = null;
        try {
            elementSpan = tracer.spanBuilder("getFromAnother")
                    .startSpan()
                    .setAllAttributes(
                            Attributes.empty()

                    );
            scope = elementSpan.makeCurrent();

            result = joinPoint.proceed();

            elementSpan.setAttribute("response", Objects.requireNonNullElse((String) result, ""));
        } catch (Throwable throwable) {
            closeSpanByError(elementSpan, throwable);
            throw throwable;
        } finally {
            if (scope != null) {
                scope.close();
            }
            if (elementSpan != null) {
                elementSpan.end();
            }
        }
        return result;
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
        } catch (Throwable throwable) {
            closeSpanByError(elementSpan, throwable);
            throw throwable;
        } finally {
            if (elementSpan != null) {
                elementSpan.end();
            }
        }
        return result;
    }

    private void closeSpanByError(Span span, Throwable throwable) {
        if (span != null) {
            span.setStatus(StatusCode.ERROR, throwable.getMessage());
            span.recordException(throwable);
            span.end();
        }
    }
}
