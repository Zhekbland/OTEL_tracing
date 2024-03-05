
# Записки разработчика. Инструмент трассировки OpenTelemetry
* [Конфигурирование кластера OpenTelemetrySDK + OpenTelemetry Collector + Tempo + Grafana UI](#конфигурирование-кластера-opentelemetrysdk--opentelemetry-collector--tempo--grafana-ui)
  * [Запуск кластера трассировки Docker-Compose](#запуск-кластера-трассировки-docker-compose)
* [OpenTelemetry кластер + примеры](#opentelemetry-кластер--примеры)
  * [Инициализация зависимостей](#инициализация-зависимостей)
  * [Базовые настройки OpenTelemetry через application.properties](#базовые-настройки-opentelemetry-через-applicationproperties)
* [Работа с трассировкой в SpringBoot RestController](#работа-с-трассировкой-в-springboot-restcontroller)
  * [OpenTelemetry - автоматический сбор трассировки](#opentelemetry---автоматический-сбор-трассировки)
  * [OpenTelemetry - автоматический сбор трассировки + вложенный вручную спан](#opentelemetry---автоматический-сбор-трассировки--вложенный-вручную-спан)
  * [OpenTelemetry - автоматический сбор трассировки + вложенный спан с помощью AOP](#opentelemetry---автоматический-сбор-трассировки--вложенный-спан-с-помощью-aop)
  * [OpenTelemetry - автоматический сбор трассировки + вложенный спан с помощью Аннотации @WithSpan](#opentelemetry---автоматический-сбор-трассировки--вложенный-спан-с-помощью-аннотации-withspan)
  * [OpenTelemetry - ГЛОБАЛЬНЫЙ автоматический сбор трассировки](#opentelemetry---глобальный-автоматический-сбор-трассировки)

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

# OpenTelemetry кластер + примеры

### Репозиторий с примерами:
Пример моего приложения можете найти тут (включает кластер для трассировки и проекты demo и demo2 (для разбора глобальной трассировки))!
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
В этом примере мы используем ДВА приложения demo и demo2. Выполняется вызов http://localhost:8090/globalTrace из проекта demo, который через RestTemplate обращается к demo2 по http://localhost:8095/microService. В свою очередь demo2 обращается к внешнему сервису  https://jsonplaceholder.typicode.com/posts/1. Тут мы наблюдаем как выполняется сбор трассировки на уровне ДВУХ сервисов и все спаны аккуратно складываются под одним трэйсом! OpenTelemetry предоставляет такой подход как [ContextPropagation](https://opentelemetry.io/docs/languages/java/instrumentation/#context-propagation).
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
