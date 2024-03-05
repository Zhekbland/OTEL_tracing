package com.example.demo.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {

    @Value("${otel.collectorUrl:http://localhost:4317}")
    private String collectorUrl;

    @Value("${otel.batchSize:512}")
    private Integer batchSize;

    @Value("${otel.batchSize:5}")
    private Long delayBatchSec;

    @Value("${otel.maxQueueSize:2048}")
    private Integer maxQueueSize;

    @Value("${otel.serviceVersion:0.1.0}")
    private String serviceVersion;

    @Value("${otel.ratio:1.0}")
    private Double ratio;

    @Value("${otel.serviceName:demo_instrumentation}")
    private String serviceName;

    @Bean
    public OpenTelemetry openTelemetry() {
        OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(collectorUrl) // URL OpenTelemetry Collector. По умолчанию "http://localhost:4317". 4317 GRPC, 4318 HTTP.
                .setCompression("gzip") // Компрессия "none"/"gzip" по умолчанию "none"
                .build();

// Настройка и инициализация пакетного процессора для батчинга данных
        BatchSpanProcessor batchSpanProcessor = BatchSpanProcessor.builder(exporter)
                .setMaxExportBatchSize(batchSize) // Максимальный размер пакета для батчинга. По умолчанию 512.
                .setScheduleDelay(delayBatchSec, TimeUnit.SECONDS) // Максимальный диапазон времени перед отправкой. По умолчанию 5000ms.
                .setMaxQueueSize(maxQueueSize) // Максимальный размер внутренней очереди. По умолчанию 2048.
                .build();

// Метаданные, ресурс-атрибуты
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.builder()
                        .put(ResourceAttributes.SERVICE_NAME, serviceName)
                        .put(ResourceAttributes.SERVICE_VERSION, serviceVersion)
                        .put(ResourceAttributes.HOST_NAME, System.getenv("HOSTNAME"))
                        .build()));

// Процент записи трассировки
        Sampler traceIdRatioBased = Sampler.traceIdRatioBased(ratio); // По умолчанию 100%.

// Настройка и инициализация SDK для OpenTelemetry
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setSampler(traceIdRatioBased)
                .setResource(resource)
                .addSpanProcessor(batchSpanProcessor)
                .build();

        return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
