
# Записки разработчика. Инструмент трассировки OpenTelemetry
* [Конфигурирование кластера OpenTelemetrySDK + OpenTelemetry Collector + Tempo + Grafana UI](#конфигурирование-кластера-opentelemetrysdk--opentelemetry-collector--tempo--grafana-ui)
  * [Запуск кластера трассировки Docker-Compose](#запуск-кластера-трассировки-docker-compose)
***
* [OpenTelemetry Autoconfiguration и Spring Boot](#opentelemetry-autoconfiguration-и-spring-boot)
  * [Инициализация зависимостей](#инициализация-зависимостей)
  * [Базовые настройки OpenTelemetry через application.properties](#базовые-настройки-opentelemetry-через-applicationproperties)
  * [Работа с трассировкой в SpringBoot RestController](#работа-с-трассировкой-в-springboot-restcontroller)
    * [OpenTelemetry - автоматический сбор трассировки](#opentelemetry---автоматический-сбор-трассировки)
    * [OpenTelemetry - автоматический сбор трассировки + вложенный вручную спан](#opentelemetry---автоматический-сбор-трассировки--вложенный-вручную-спан)
    * [OpenTelemetry - автоматический сбор трассировки + вложенный спан с помощью AOP](#opentelemetry---автоматический-сбор-трассировки--вложенный-спан-с-помощью-aop)
    * [OpenTelemetry - автоматический сбор трассировки + вложенный спан с помощью Аннотации @WithSpan](#opentelemetry---автоматический-сбор-трассировки--вложенный-спан-с-помощью-аннотации-withspan)
    * [OpenTelemetry - ГЛОБАЛЬНЫЙ автоматический сбор трассировки](#opentelemetry---глобальный-автоматический-сбор-трассировки)
***
* [OpenTelemetry instrumentation - ручная конфигураци и Spring Boot или Java SE](#opentelemetry-instrumentation---ручная-конфигураци-и-spring-boot-или-java-se)
  * [Инициализация зависимостей для Java SE или SpringBoot](#инициализация-зависимостей-для-java-se-или-springboot)
  * [Инициализация компонентов OpenTelemetrySDK](#инициализация-компонентов-opentelemetrysdk)
  * [OpenTelemetry Instrumentation - ручная работа с трассировкой в SpringBoot](#opentelemetry-instrumentation---ручная-работа-с-трассировкой-в-springboot)
    * [OpenTelemetry - сбор трассировки вручную](#opentelemetry---сбор-трассировки-вручную)
    * [OpenTelemetry - сбор трассировки вручную с помощью AOP](#opentelemetry---сбор-трассировки-вручную-с-помощью-aop)
# Конфигурирование кластера OpenTelemetrySDK + OpenTelemetry Collector + Tempo + Grafana UI

#### Тут скоро появиться дока по параметрам конфигураций)
### Запуск кластера трассировки Docker-Compose.
Для запуска тестового кластера в Docker-Compose можно воспользоваться заготовками от меня (docker-compose кластер - включает кластер для трассировки и demo проект). В данном репозитории доступно всё для удобного запуска кластера трассировки и тестирования, достаточно скачать и запустить используя средства Docker-Compose. Состав кластера включает:

* otel-collector - коллектор от OpenTelemetry
* tempo - Tempo))
* grafana - Grafana UI
* k6-tracing - инструмент автоматически генерирует рандомные запросы в коллектор. Его достаточно запустить.

Выполняется отправка тестовых данных из k6-tracing → otel-collector → Tempo → Grafana UI. Данные трассировки в Tempo и настроек Grafana хранятся локально в папке репозитория для использования в doker.

Для запуска:

* git clone docker-compose кластер
* создаем сеть - docker network create otelnetwork
* запускаем кластер - docker-compose up
* доступ к Grafana по порту - http://localhost:3000/. Логин/пароль - admin/admin
* доступ к метрикам Tempo по порту - http://localhost:3200/metrics.
* от otel-collector доступен порт gRPC - 4317, для отправки трассировки из своего приложения средствами OpenTelemetry SDK - Применение OpenTelemetry SDK. Не забудь отключить k6-tracing, чтобы не спамил!

В Grafana UI выбираем хранилище Tempo и можем просматривать трассировку через Search и делать запросы через Trace QL и смотреть/создавать Dashboard. Перечень действий на скринах:

<img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/docker/grafana/img1.png'>

<img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/docker/grafana/img2.png'>

<img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/docker/grafana/img3.png'>

<img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/docker/grafana/img4.png'>

***
***

# OpenTelemetry Autoconfiguration и Spring Boot

### Репозиторий с примерами:
Пример моего приложения можете найти тут (включает кластер для трассировки и проекты [demo](https://github.com/zhekbland/OTEL_tracing/tree/main/demo) и [demo2](https://github.com/zhekbland/OTEL_tracing/tree/main/demo2) (для разбора глобальной трассировки))!
***
### Инициализация зависимостей
Инициализация OpenTelemetry SDK в SpringBoot v3 ([офф. док.](https://opentelemetry.io/docs/languages/java/automatic/spring-boot/))
* Пример - [build.gradle](https://github.com/zhekbland/OTEL_tracing/tree/main/demo/build.gradle)
***
### Базовые настройки OpenTelemetry через application.properties
Выполнять настройки телеметрии можно через application.properties (перечень свойств для конфигурации - ([gitHub](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure), [офф. док.](https://opentelemetry.io/docs/languages/java/automatic/configuration/))
* Пример - [application.properties](https://github.com/zhekbland/OTEL_tracing/tree/main/demo/src/main/resources/application.properties)

```properties
server.port=8090
otel.sdk.disabled=false
otel.logs.exporter=none
otel.metrics.exporter=none
otel.traces.exporter=otlp
otel.exporter.otlp.endpoint=http://localhost:4317
otel.exporter.otlp.protocol=grpc
otel.service.name=example-app
```
***

### Работа с трассировкой в SpringBoot RestController

#### OpenTelemetry - автоматический сбор трассировки
По умолчанию opentelemetry-spring-boot-starter выполняет обработку endpoints и оборачивает методы в котроллерах автоматически с вложенностью! В этом примере мы дополнительно задействовали RestTemplate для обращения к внешнему сервису. Использование глобальной трассировки на уровне микросервисов будет рассмотрено ниже!
* Пример кода - [ControllerExample](https://github.com/zhekbland/OTEL_tracing/tree/main/demo/src/main/java/com/example/demo/controller/ControllerExample.java)
    ```java
    @RestController
    public class ControllerExample {
        private final RestTemplate restTemplate;
        public ControllerExample(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }
        @GetMapping("/trace")
        public String getFromResource() {
            String response = null;
            response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/post return response;
        }
    }
    ```
    http://localhost:8090/trace


* Пример трэйса

    <img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/rest/img1.png'>
***

#### OpenTelemetry - автоматический сбор трассировки + вложенный вручную спан
Так же мы можем вызвать экземпляр OpenTelemetry, созданный автоматически, и выполнить свои сборы трассировки! Как видно из кода, мы вызываем метод customMethod и самостоятельно оборачиваем его в спан. Так жe, дополнительно, 3 раза выполнили обращение через restTemplate. В итоге автоматически спаны собраны в трэйс, как видно на скрине ниже. **Никто не заставляет вас так использовать трассировку вручную и оборачивать вложенные методы самостоятельно или с помощью AOP - это всего лишь пример))**
* Пример кода - [ControllerExample](https://github.com/zhekbland/OTEL_tracing/tree/main/demo/src/main/java/com/example/demo/controller/ControllerExample.java)
    ```java
    @RestController
    public class ControllerExample {

        private final RestTemplate restTemplate;
        private final Tracer tracer;

        public ControllerExample(OpenTelemetry openTelemetry, RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
            this.tracer = openTelemetry.getTracer(ControllerExample.class.getName(), "0.1.0");
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

    }
    ```
  http://localhost:8090/customTrace


* Пример трэйса

    <img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/rest/img2.png'>
***

#### OpenTelemetry - автоматический сбор трассировки + вложенный спан с помощью AOP
В этом примере мы используем средства AOP. Создав TracingAspect, с помощью AOP выполняем обертку над методом и сбор трассировки. **Никто не заставляет вас так использовать трассировку вручную и оборачивать вложенные методы самостоятельно или с помощью AOP - это всего лишь пример))**
* Пример кода - [ControllerExample](https://github.com/zhekbland/OTEL_tracing/tree/main/demo/src/main/java/com/example/demo/controller/ControllerExample.java)
    ```java
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
    
        @GetMapping("/customAopTrace")
        public String getFromAnotherWithAop() {
            String response = null;
    
            service.customMethod(250);
    
            response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", String.class);
    
            return response;
        }
    
    }
    ```
* Пример AOP кода - [TracingAspect](https://github.com/zhekbland/OTEL_tracing/tree/main/demo/src/main/java/com/example/demo/aop/TracingAspect.java)
  ```java
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
  ```
  http://localhost:8090/customAopTrace


* Пример трэйса

    <img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/rest/img3.png'>

***

#### OpenTelemetry - автоматический сбор трассировки + вложенный спан с помощью Аннотации @WithSpan
Так же мы можем вызвать экземпляр OpenTelemetry, созданный автоматически, и выполнить свои сборы трассировки c помощью Аннотации @WithSpan! Как видно из кода, мы вызываем метод customMethodWithAnnotation и используем аннотацию @WithSpan для трассировки метода и аннотацию @SpanAttribute для добавления атрибутов. И в результате спан собранный с помощью аннотации @WithSpan отлично вкладывается в общий Scope нашего endpoint.
* Пример кода - [ControllerExample](https://github.com/zhekbland/OTEL_tracing/tree/main/demo/src/main/java/com/example/demo/controller/ControllerExample.java)
* Пример кода - [ExampleService](https://github.com/zhekbland/OTEL_tracing/tree/main/demo/src/main/java/com/example/demo/service/ExampleService.java)
    ```java
    // Контроллер
    @GetMapping("/customTraceWithAnnotation")
    public String getFromAnotherWithAnnotation() {
        String response = null;
    
        service.customMethodWithAnnotation(300);
    
        for (int i = 1; i < 4; i++) {
            response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/" + i, String.class);
        }
    
        return response;
    }
    
    // Метод Сервиса
    @WithSpan("customWithAnnotation")
    public void customMethodWithAnnotation(@SpanAttribute("delayMs") Integer delayMs) {
      try {
        Thread.sleep(delayMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    ```
  http://localhost:8090/customTraceWithAnnotation


* Пример трэйса

    <img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/rest/img5.png'>
  
    И атрибуты, которые мы добавили.

    <img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/rest/img6.png'>

***

#### OpenTelemetry - ГЛОБАЛЬНЫЙ автоматический сбор трассировки
В этом примере мы используем ДВА приложения [demo](https://github.com/zhekbland/OTEL_tracing/tree/main/demo) и [demo2](https://github.com/zhekbland/OTEL_tracing/tree/main/demo2). Выполняется вызов http://localhost:8090/globalTrace из проекта demo, который через RestTemplate обращается к demo2 по http://localhost:8095/microService. В свою очередь demo2 обращается к внешнему сервису  https://jsonplaceholder.typicode.com/posts/1. Тут мы наблюдаем как выполняется сбор трассировки на уровне ДВУХ сервисов и все спаны аккуратно складываются под одним трэйсом! OpenTelemetry предоставляет такой подход как [ContextPropagation](https://opentelemetry.io/docs/languages/java/instrumentation/#context-propagation).
* Пример кода
    ```java
    //DEMO
    
    @GetMapping("/globalTrace")
    public String getFromAnotherService() {
        String response = null;
    
        service.customMethod(250);
    
        response = restTemplate.getForObject("http://localhost:8095/microService", String.class);
    
        return response;
    }
    
    //DEMO2
    
    @GetMapping("/microService")
    public String getFromResource() {
        String response = null;
    
        response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", String.class);
    
        return response;
    }
    ```
  http://localhost:8090/globalTrace


* Пример трэйса

    <img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/rest/img4.png'>



# OpenTelemetry instrumentation - ручная конфигураци и Spring Boot или Java SE

### Репозиторий с примерами:
Пример моего приложения можете найти тут [demo_instrumentation](https://github.com/zhekbland/OTEL_tracing/tree/main/demo_instrumentation)!

***

### Инициализация зависимостей для Java SE или SpringBoot
Ручная инициализация библиотек OpenTelemetry SDK в SpringBoot v3 ([офф. док.](https://opentelemetry.io/docs/languages/java/instrumentation/#manual-instrumentation-setup))
* Пример - [build.gradle](https://github.com/zhekbland/OTEL_tracing/tree/main/demo_instrumentation/build.gradle) (закоментированные зависимости потребуются при использовании OpenTelemetry с Java SE)

***

### Инициализация компонентов OpenTelemetrySDK.
Можно воспользоваться документацией или заготовкой ниже.
Инициализации трассировки из официальной документации [пример](https://github.com/open-telemetry/opentelemetry-java-examples/blob/main/sdk-usage/src/main/java/io/opentelemetry/sdk/example/ConfigureSpanProcessorExample.java) (актуальный код примера поддерживается и обновляется).
* Код для инициализации трассировки - [RestTemplateConfig](https://github.com/zhekbland/OTEL_tracing/tree/main/demo/src/main/java/com/example/demo/controller/ControllerExample.java)

```java
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

    // Настройка и инициализация Exporter для отправки данных в OpenTelemetry Collector GRPC  
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
}
```

Параметры в **OtlpGrpcSpanExporter**:

* **setCompression** - установка компресси при отправке пакетов со спанами. По умолчанию "none", для влкючения компрессии указывается "gzip".


Важные параметры конфигурируются в **BatchSpanProcessor**:

* **setMaxExportBatchSize** - устанавливается максимальный размер передаваемого пакета во время батчинга. Значение равно кол-ву спанов. Параметр напрямую зависит от **setScheduleDelay***.
* **setScheduleDelay** - устанавливает время между "батчингами", отправкой пакетов спанов в OpenTelemetry Collector. Параметр напрямую зависит от **setMaxExportBatchSize***.
* **setMaxQueueSize** - устанавливает размер внутренней очереди BatchSpanProcessor. Внутренняя очередь - это буфер, необходимый для сохранения тех спанов, которые не успели отправиться во время батчинга (отправки пакета спанов). Если размер очереди мaxQueueSize переполняется, то происходит удаление спанов, которые должны были попасть в данную очередь.

*Зависимость параметров maxExportBatchSize, scheduleDelay. Тут всё просто, если прошёл установленный диапазон времени scheduleDelay, тогда происходит отправка, даже если maxExportBatchSize не достиг указанного значения. Иначе если maxExportBatchSize достиг установленного значения, тогда выполняется отправка, даже если scheduleDelay не достиг установленного диапазона времени!

*maxExportBatchSize - имеет неявный предел в зависимости от размера одного спана. Без установленной компрессии в параметре setCompression("gzip") - maxExportBatchSize = 40000-50000 достигает предела размера пакета GRPC в 4Мб.


**! Если сообщения генерируются с такой скоростью/частотой что батчинг и его различные вариации настроек(maxExportBatchSize, scheduleDelay) не успевают осуществлять отправку и мaxQueueSize постоянно заполняется и/или переполняется, то технология трассировки вероятно не ваш выбор).**



Параметр сэмплирования **Sampler**:

* **traceIdRatioBased** - устанавливается в диапазоне от 0.1 до 1.0, что эквивалентно 10% - 100%. То есть кол-во трассируемой информации. По умолчанию 100%.


**Советы.**

* Для трассировки достаточно создать экземпляр SdkTracerProvider tracerProvider.
* Если создавать OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();  то он определяет свой SdkTracerProvider, который как показала практика, имеет приоритет над собственно-созданными экземплярами SdkTracerProvider. И применяет параметры из SdkTracerProvider от OpenTelemetrySdk.
* Если создавать OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build() именно через build(), то OpenTelemetrySdk можно переопределить во время работы приложения. Если использовать buildAndRegisterGlobal(), в таком случае OpenTelemetrySdk станет глобальным и его нельзя переопределить стандартными средствами.

Полноценные примеры и обновляемый код можно найти в репозитории от создателей OpenTelemetry [gitHub](https://github.com/open-telemetry/opentelemetry-java-examples/tree/main/sdk-usage/src/main/java/io/opentelemetry/sdk/example).

***

### OpenTelemetry Instrumentation - ручная работа с трассировкой в SpringBoot

#### OpenTelemetry - сбор трассировки вручную
В этом примере мы используем средства AOP для обертки метода контроллера - [getFromAnother()](https://github.com/zhekbland/OTEL_tracing/tree/main/demo_instrumentation/src/main/java/com/example/demo/controller/ControllerExample.java). Создав TracingAspect, с помощью AOP выполняем обертку над методом контроллера, создание scope и сбор трассировки. Так же внутри метода демонстрируется создание спанов вручную, создание scope для rootSpan и создание дочерних спанов (вложеных в rootSpan). Ниже примеры кода. **Никто не заставляет вас так использовать трассировку вручную и оборачивать вложенные методы самостоятельно или с помощью AOP - это всего лишь пример))**

Лучше использовать данный подход [OpenTelemetry - сбор трассировки вручную с помощью AOP](#opentelemetry---сбор-трассировки-вручную-с-помощью-aop), но для примера расмотрим и такой-гибридный.
* Пример кода - [ControllerExample](https://github.com/zhekbland/OTEL_tracing/tree/main/demo_instrumentation/src/main/java/com/example/demo/controller/ControllerExample.java)
    ```java
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
    
        @GetMapping("/customTrace")
        public String getFromAnother() {
            String response = null;
    
            Span parentSpan = tracer.spanBuilder("My custom method").startSpan();
            service.customMethod(parentSpan);
            parentSpan.end();
    
            Span rootSpan = tracer.spanBuilder("rootSpan").startSpan();
            try (Scope scope = rootSpan.makeCurrent()) {
                for (int i = 1; i < 4; i++) {
                    Span childSpan = tracer.spanBuilder("childSpan").startSpan();
                    response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/" + i, String.class);
                    childSpan.setAttribute("response", Objects.requireNonNullElse(response, ""));
                    childSpan.end();
                }
            } finally {
                rootSpan.end();
            }
    
            return response;
        }
    
    }
    ```
  * Пример AOP кода - [TracingAspect](https://github.com/zhekbland/OTEL_tracing/tree/main/demo_instrumentation/src/main/java/com/example/demo/aop/TracingAspect.java)
  ```java
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
  
    private void closeSpanByError(Span span, Throwable throwable) {
      if (span != null) {
        span.setStatus(StatusCode.ERROR, throwable.getMessage());
        span.recordException(throwable);
        span.end();
      }
    }
  }
  ```
    http://localhost:8888/customTrace


* Пример трэйса. На скрине видно что создался глобальный трэйс/спан для метода контроллера getFromAnother(), также обернулся наш кастомный метод и rootSpan с child, внутри которых мы обращаемся к внешнему сервису https://jsonplaceholder.typicode.com/posts/1.

    <img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/instrumentation/img1.png'>

* На данном скрине видно, что в атрибуты мы вложили ответ от вызова внешнего сервиса https://jsonplaceholder.typicode.com/posts/1.

    <img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/instrumentation/img2.png'>

***

#### OpenTelemetry - сбор трассировки вручную с помощью AOP
В этом примере мы полноценно используем средства AOP для обертки метода контроллера - [getFromAnotherWithAop()](https://github.com/zhekbland/OTEL_tracing/tree/main/demo_instrumentation/src/main/java/com/example/demo/controller/ControllerExample.java) и всех вложенного метода. Вся магия происходит в классе [TracingAspect](https://github.com/zhekbland/OTEL_tracing/tree/main/demo_instrumentation/src/main/java/com/example/demo/aop/TracingAspect.java). Создав TracingAspect, с помощью AOP выполняем обертку над методом контроллера, создание scope и сбор трассировки. Так же в следующем аспекте выполняется обертка и сбор трассировки над методом customMethod(Integer delayMs). Ниже примеры кода.
* Пример кода - [ControllerExample](https://github.com/zhekbland/OTEL_tracing/tree/main/demo_instrumentation/src/main/java/com/example/demo/controller/ControllerExample.java). Теперь метод не имеет лишнего кода и логики благодаря AOP.
    ```java
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
    
        @GetMapping("/customAopTrace")
        public String getFromAnotherWithAop() {
            String response = null;
    
            service.customMethod(250);
    
            response = restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", String.class);
    
            return response;
        }
    
    }
    ```
  * Пример AOP кода - [TracingAspect](https://github.com/zhekbland/OTEL_tracing/tree/main/demo_instrumentation/src/main/java/com/example/demo/aop/TracingAspect.java)
  ```java
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
  ```
  http://localhost:8888/customAopTrace


* Пример трэйса. На скрине видно что создался глобальный трэйс/спан для метода контроллера getFromAnotherWithAop(), также обернулся наш кастомный метод. Внутри метода контроллера getFromAnotherWithAop() мы обращаемся к внешнему сервису https://jsonplaceholder.typicode.com/posts/1, по этому заметна общая длительность главного спана, большая чем дочернего.

    <img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/instrumentation/img3.png'>

* На данном скрине видно, что в атрибуты главного спана мы вложили ответ от вызова внешнего сервиса https://jsonplaceholder.typicode.com/posts/1.

    <img src='https://github.com/zhekbland/OTEL_tracing/blob/main/pic/instrumentation/img4.png'>

***