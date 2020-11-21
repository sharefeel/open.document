# Google Cloud Compute Engine Basic

## 준비

Compute engine에서 실행될 spring boot rest api code

```java
@SpringBootApplication
public class HelloApp {
    public static void main(String... args) {
        SpringApplication.run(HelloApp.class);
    }
}

@RestController
@RequestMapping("/")
public class HelloController {
    @GetMapping("/hello")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("Hello World");
    }
}
```

## Compute Engine 생성

### Instance Template

Compute Engine > 인스턴스 템플릿 > 인스턴스 템플릿 만들기

- 이름: hellorest-instance-template
- e2-small
- Debian 18.04 10GB disk
- 기본 액세스 허용
- HTTP 트래픽 허용

