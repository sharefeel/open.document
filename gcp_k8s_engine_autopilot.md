# GCP Kubernetes Engine AutoPilot Mode

2021/04/03 기준 GKE에서는 두가지 방법으로 클러스터를 생성할 수 있다.

1. 기본(?)
2. Auto Pilot

사용의 간편함부터 과금 단위까지 여러가지 차이가 있다. 자세한건 auto pilot 문서를 참고하라. 그냥 문서 링크.
이 문서에서는 Auto Pilot을 다룰 것인데 그 이유는 다음과 같다.

- Pod 단위만 생각하면 되므로 쉽다
- 클러스터의 한가지 표준을 볼 수 있다.

## 일단 Kubernetes 가 뭔지는 아래 링크 참고

참고할만한 링크

### Auto Pilot 모드에 생성되는 클러스터

Auto pilot으로 생성하면 도대체 어떤 형태로 클러스터가 

## 배포할 컨테이너

매우 간단한 spring-boot code 이다.

```java
@RestController
@RequestMapping("/")
@SpringBootApplication
public class HelloRestApp {

    public static void main(String... args) {
        SpringApplication.run(HelloRestApp.class);
    }

    @GetMapping("hello")
    public ResponseEntity<String> hello() throws UnknownHostException {
        String localAddress = InetAddress.getLocalHost().getHostAddress();
        String localHostName = InetAddress.getLocalHost().getHostName();
        return ResponseEntity.ok("GitHub ver 2. I am " + localHostName + "(" + localAddress + ") ");
    }

    @GetMapping("alive")
    public ResponseEntity<String> alive() {
        return ResponseEntity.ok("alive ");
    }
}
```

아래는 cloud build 에 의해서 실행될 Dockerfile이다. Java 11 상에서 위 프로그램을 동작시킨다.

```Dockerfile
# Build stage
FROM maven:3-openjdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -Dmaven.test.skip=true

# Package stage
FROM gcr.io/distroless/java:11
COPY --from=build /home/app/target/hellorest-github.jar /usr/local/lib/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/app.jar"]
```
