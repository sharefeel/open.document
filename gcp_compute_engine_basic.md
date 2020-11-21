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

### Instance Template

Compute Engine > 인스턴스 템플릿 > 인스턴스 템플릿 만들기

- 이름: hellorest-instance-template
- e2-small
- Ubuntu 18.04 10GB disk
- 기본 액세스 허용
- HTTP 트래픽 허용

### VM 인스턴스 구성

Compute Engine > VM 인스턴스 > 만들기 > 템플릿에서 VM 인스턴스 만들기.
위에서 생성해둔 hellorest-instance-template 템플릿으로 계속

- 이름: hellorest-instance
- 리전: asia-northeast3
- 영역: asia-northeast3-1

설정 후 SSH 접속하여 VM 구성

#### 설치

1. apt install default-jre
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

## 이미지 기반으로 VM 인스턴스 생성

Compute Engine > 머신 이미지 > 머신 이미지 만들기

다음 내용으로 create

- 이름: hellorest-instance-image
- 소스 VM 인스턴스: hellorest-instance
- 위치: 리전 / asia-northeast3(서울)

대기.. 이 작업은 좀 오래 걸림
18:21 에 시작했는데 끝나질 않음..
미리 만들어뒀음

## 인스턴스 그룹 생성

Compute Engine > 인스턴스 그룹 만들기 > 새로운 스테이트리스 관리형 인스턴스 그룹

다음 내용으로 , hellorest-group2 의 두 인스턴스 그룹 생성

- 이름: hellorest-group1
- 단일 영역, asia-northeast3(서울), asia-northeast3-a
- hellorest-instance-templat
- 인스턴스의 최대 개수: 3

- 이름: hellorest-group2
- 단일 영역, asia-northeast3(서울), asia-northeast3-b
- hellorest-instance-templat
- 인스턴스의 최대 개수: 3

## VPC 생성

- 이름: hellorest-vpc
- VPC
  - 이름: hellorest-subnet
  - 리전: asia-northeast3
  - IP 주소범위: 10.0.0.0/9
