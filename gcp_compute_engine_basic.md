# Google Cloud Compute Engine Basic

## 구성할 것

### 형상

- java rest api on compute engine vm
- 리전내 두 영역에 걸쳐서 서버 배포
- 로드밸런싱

### 구성 절차

1. 준비단계
   1. 배포할 VM 구성
   2. 구성한 VM의 스토리지 이미지 생성
   3. 디스크 이미지 기반의 인스턴스 템플릿 생성
2. 실제 구성
   1. 템플릿 기반으로 인스턴스 그룹 생성
   2. 환경 구성: 방화벽, health check
   3. 인스턴스 그룹으로 트래픽을 보내는 로드밸런서 생성

## Java 앱 준비

### 실행할 java 앱

Compute engine에서 실행될 spring boot rest api code. 현재 실행된 장비의 호스트네임과 IP를 리턴한다.

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
    @GetMapping("hello")
    public ResponseEntity<String> helloWorld() throws UnknownHostException {
        String localAddress = InetAddress.getLocalHost().getHostAddress();
        String localHostName = InetAddress.getLocalHost().getHostName();
        return ResponseEntity.ok("Hello. I am " + localHostName + "(" + localAddress + ")");
    }
}
```

### 앱을 GCS에 업로드

Spring boot embedded tomcat .jar 로 패키징후 gcs 에 업로드. 파일명: hellorest-0.0.1.jar

Storage > 버킷 만들기

- 버킷이름: ce-resource
- 위치: Region, asia-northeast3
- 그외 기본값

버킷 최상위 경로에 .jar 파일 업로드

## Compute Engine 생성

### VM 인스턴스 구성

Compute Engine > VM 인스턴스 > 만들기 > VM 인스턴스 만들기.

- 이름: hellorest-ce
- 리전: asia-northeast3
- 영역: asia-northeast3-a

설정 후 SSH 접속하여 VM 구성

#### 설치

java 설치

```bash
sudo apt install default-jre
```

downloadAndrun.sh 스크립트를 /home/sharefeel 경로에 생성. GCS 에서 jar 파일 다운로드 후 실행하는 스크립트임

```bash
cat downloadAndRun.sh
pushd $(dirname $0) > /dev/null
/snap/bin/gsutil cp gs://ce-resource/hellorest-0.0.1.jar .
/usr/bin/java -jar hellorest-0.0.1.jar
popd
```

#### 서비스 등록

다음내용으로 /etc/systemd/system/hellorest.service 생성

```bash
[Unit]
Description=Hello Rest App Service

[Service]
WorkingDirectory=/home/sharefeel
ExecStart=/bin/bash /home/sharefeel/downloadAndRun.sh
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
:
:
```

호출 테스트

```bash
curl http://localhost:8080/hello
Hello. I am hostname(ipaddress)
```

#### 리부팅 후 자동 실행되는지 테스트

1. hellorest-ce 중지
2. hellorest-ce 시작
3. SSH 접속후 확인
   - sudo systemctl status hellorest.service 또는
   - curl http://localhost:8080/hello

#### 인스턴스 중지

더이상 이 인스턴스는 필요하지 않다. 돈드니까 그냥 중지하자.

Compute Engine > VM 인스턴스

hellorest-ce VM 중지

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

### 상태 확인을 위한 방화벽 규칙 만들기

VPC 네트워크 > 방화벽 > 방화벽 규칙 만들기

- 이름: allow-health-check
- 대상: 네트워크의 모든 인스턴스
- 소스IP 범위: 130.211.0.0/22 35.191.0.0/16
- 지정된 프로토콜 및 포트: tcp/8080

### 인스턴스 그룹 생성

Compute Engine > 인스턴스 그룹 만들기 > 새로운 스테이트리스 관리형 인스턴스 그룹

- 인스턴스 그룹 1
  - 이름: hellorest-ce-group1
  - asia-northeast3(서울), asia-northeast3-b
  - hellorest-ce-template
  - 인스턴스의 최대 개수: 2
  - 자동복구: hellorest-ce-state (HTTP, 8080) /hello 600초
- 인스턴스 그룹 2
  - 이름: hellorest-ce-group2
  - asia-northeast3(서울), asia-northeast3-c
  - hellorest-ce-template
  - 인스턴스의 최대 개수: 2
  - 자동복구 hellorest-ce-state (HTTP, 8080) /hello 600초

이때 주의할 점은 잠재적으로 생생될 수 있는 최대 인스턴스 수가 쿼터보다 작아야 한다. 즉 위 두개의 인스턴스 그룹을 만들면 최대 인스턴스 쿼터를 6개 먹고 들어간다.

## 부하 분산기 만들기

네트워크 서비스 > 부하 분산 > 부하 분산기 만들기 > HTTP(S) 부하 분산 > 인터넷 트래픽을 VM으로 분산

- 이름: hellorest-ce-load-balancer
- 백엔드 서비스
  - 이름: hellorest-ce-backend-service
  - 백엔드 유형: 인스턴스 그룹
  - 백엔드
    - hellorest-ce-group1 (asia-northeast3-b) / 8080 포트
    - hellorest-ce-group2 (asia-northeast3-c) / 8080 포트
  - 상태 확인: hellorest-ce-state (HTTP)
- 호스트 및 경로 규칙: 단순한 호스트 및 경로 규칙
- 프런트 엔드 구성
  - 이름: hellorest-ce-front-end
  - 포트: 8080

## 테스트

Http 호출을 처리하는 서버가 fail-over, recovery 되는 것을 다음과 같이 확인할 수 있다.

1. http://IP:8080/hello 호출 (참고. IP는 부하 분산기 정보에서 얻을 수 있음)
2. 리턴된 IP 에 SSH 접속하여 서비스 종료 (sudo systemctl stop hellorest.service)
3. http://IP:8080/hello 호출하여 다른 인스턴스로 라우팅 되었는지 확인
4. 서비스 종료한 인스턴스가 종료되는 것 확인
5. 인스턴스가 사라진 인스턴스 그룹에 다시 인스턴스가 생성되는 것 확인
