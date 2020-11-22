# Google Cloud Compute Engine Basic

## 준비

### 실행할 java 앱

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

### 앱을 GCS에 업로드

.jar 로 패키징후 gcs 에 업로드

Storage > 버킷 만들기

- 버킷이름: ce-resource
- 위치: Region, asia-northeast3
- 그외 기본값

위 설정으로 버킷 생성후 .jar 파일 업로드. (hellorest-0.0.1.jar 로 가정)

## Compute Engine 생성

### VM 인스턴스 구성

Compute Engine > VM 인스턴스 > 만들기 > 템플릿에서 VM 인스턴스 만들기.
위에서 생성해둔 hellorest-instance-template 템플릿으로 계속

- 이름: hellorest-instance
- 리전: asia-northeast3
- 영역: asia-northeast3-a

설정 후 SSH 접속하여 VM 구성

#### 설치

1. sudo apt install default-jre
2. gsutil cp gs://ce-resource/hellorest-0.0.1.jar .

#### 서비스 등록

다음내용으로 /etc/systemd/system/hellorest.service 생성

```bash
[Unit]
Description=Hello Rest App Service

[Service]
WorkingDirectory=/home/sharefeel
ExecStart=/usr/bin/java -jar /home/sharefeel/hellorest-0.0.1.jar
ExecStop=/bin/kill -INT $MAINPID
ExecReload=/bin/kill -TERM $MAINPID

[Install]
WantedBy=multi-user.target
Alias=hellorest.service
```

실행 후 확인 테스트

```bash
sudo systemctl daemon-reload
sudo systemctl enable hellorest
sudo systemctl start hellorest
```

```bash
sudo systemctl status hellorest
● hellorest.service - Hello Rest App Service
   Loaded: loaded (/etc/systemd/system/hellorest.service; enabled; vendor preset: enabled)
   Active: active (running) since Sat 2020-11-21 08:07:59 UTC; 6s ago
 Main PID: 9080 (java)
    Tasks: 34 (limit: 2332)
   CGroup: /system.slice/hellorest.service
           └─9080 /usr/bin/java -jar /home/sharefeel/hellorest-0.0.1.jar
Nov 21 08:08:01 hellorest-instance java[9080]: 2020-11-21 08:08:01.161  INFO 9080 --- [           main] org.example.hellorest.HelloApp           : Starting Hel
Nov 21 08:08:01 hellorest-instance java[9080]: 2020-11-21 08:08:01.169  INFO 9080 --- [           main] org.example.hellorest.HelloApp           : No active pr
Nov 21 08:08:03 hellorest-instance java[9080]: 2020-11-21 08:08:03.464  INFO 9080 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initi
Nov 21 08:08:03 hellorest-instance java[9080]: 2020-11-21 08:08:03.483  INFO 9080 --- [           main] o.apache.catalina.core.StandardService   : Starting ser
Nov 21 08:08:03 hellorest-instance java[9080]: 2020-11-21 08:08:03.483  INFO 9080 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Ser
Nov 21 08:08:03 hellorest-instance java[9080]: 2020-11-21 08:08:03.605  INFO 9080 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing
Nov 21 08:08:03 hellorest-instance java[9080]: 2020-11-21 08:08:03.605  INFO 9080 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebAppl
Nov 21 08:08:03 hellorest-instance java[9080]: 2020-11-21 08:08:03.943  INFO 9080 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing
Nov 21 08:08:04 hellorest-instance java[9080]: 2020-11-21 08:08:04.366  INFO 9080 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat start
Nov 21 08:08:04 hellorest-instance java[9080]: 2020-11-21 08:08:04.398  INFO 9080 --- [           main] org.example.hellorest.HelloApp           : Started Hell
```

호출 테스트

```bash
curl http://localhost:8080/hello
Hello World
```

#### 리부팅 후 자동 실행되는지 테스트

1. hellorest-instance 중지
2. hellorest-instance 시작
3. SSH 접속후 확인
   - sudo systemctl status hellorest.service 또는
   - curl http://localhost:8080/hello

### 머신 이미지 생성

Compute Engine > 머신 이미지 > 머신 이미지 만들기

다음 내용으로 create. 이작업은 굉장히 오래 걸린다. (한시간 이상)

- 이름: hellorest-ce-image
- 소스 VM 인스턴스: hellorest-ce
- 위치: 리전, asia-northeast3(서울)

### 스토리지 이미지 생성

Compute Engine > 이미지 (스토리지) > 이미지 만들기

- 이름: hellorest-ce-storage-image
- 소스디스크: hellorest-ce
- 위치: 지역 / asia-northeast3(서울)

### 인스턴스 템플릿 생성

Compute engine > 인스턴스 템플릿 > 인스턴스 템플릿 만들기

- 이름: hellorest-ce-instance-template
- 머신 유형: e2-small
- 부팅디스크: 맞춤이미지, hellorest-ce-storage-image 으로 변경
- 방화벽: http 트래픽 허용

### 상태 확인 생성

장비가 정상적으로 동작하는지 확인하기 위한 상태확인 등록 (health-check)

Compute Engine > 상태 확인 > 상태 확인 만들기

- 이름: hellorest-ce-state
- 프로토콜: HTTP
- 포트: 8080
- 요청경로: /hello

### 상태 확인을 위한 방화벽 규칙 만들기

VPC 네트워크 > 방화벽 > 방화벽 규칙 만들기

- 이름: allow-health-check
- 대상: 네트워크의 모든 인스턴스
- 소스IP 범위: 130.211.0.0/22 35.191.0.0/16
- 지정된 프로토콜 및 포트: tcp/8080

### 인스턴스 그룹 생성

Compute Engine > 인스턴스 그룹 만들기 > 새로운 스테이트리스 관리형 인스턴스 그룹

- 이름: hellorest-ce-group1
- asia-northeast3(서울), asia-northeast3-b
- hellorest-ce-template
- 인스턴스의 최대 개수: 3
- 자동복구 상태확인: hellorest-ce-state (HTTP)

- 이름: hellorest-ce-group2
- asia-northeast3(서울), asia-northeast3-c
- hellorest-ce-template
- 인스턴스의 최대 개수: 3
- 자동복구 상태확인: hellorest-ce-state (HTTP)

## VPC 생성

- 이름: hellorest-vpc
- VPC
  - 이름: hellorest-subnet
  - 리전: asia-northeast3
  - IP 주소범위: 10.0.0.0/9
