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

### Auto Pilot으로 클러스터 생성

콘솔에서 auto pilot으로 생성하는 화면으로 이름과 리전만 설정하면 된다. 입력 내용이 중요한 것은 아니고 autopilot 이 무엇을 해주는지 한번 읽어보자.

![create autopilot cluster](.resources/gcp_k8s_engine_autopilot/create_cluster_01.png)

실제 생성된 클러스터 설정은 다음과 같다. 여러 설정이 기본으로 세팅되어 있다.

<details> <summary> cluster 상세 설정 (펼쳐보기) </summary>

![created cluster](.resources/gcp_k8s_engine_autopilot/created_cluster_01.png)
![created cluster](.resources/gcp_k8s_engine_autopilot/created_cluster_02.png)

</details>

### 배포할 컨테이너

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

### Cloud Build

Dockerfile 빌드후 gcr 업로드

위 소스코드는 github에 업로드 되어 있다.

다음 정보로 build trigger 생성

- `이름` hellorest-trigger
- `태그` k8s-autopilot (필수 입력 아님)
- `트리거` 수동호출
- `소스`
  - `저장소` sharefeel/hellorest
  - `버전` 브랜치 / main
- `구성`
  - `유형` Dockerfile
  - `위치` 저장소
  - `Dockerfile 디렉토리` / (default)
  - `Dockerfile 이름` Dockerfile (default)
  - `이미지 이름` gcr.io/youngrok/github.com/sharefeel/hellorest:$COMMIT_SHA (default)

Github main branch 를 가져와 docker 빌드하는 것으로써 설정상 특별한 것은 없다. 알아둘 것은 가장 마지막행 `이미지 이름` 부분으로 이 이름으로 container registry 에 push 된다.

### 배포

kubernetes engine > deploy

1. `컨테이너`
   1. 기존 컨테이너 이미지 (default)
   2. container registry에 업로드된 이미지 선택 - gcr.io/youngrok/github.com/sharefeel/hellorest@sha256:...
2. `구성`
   1. `애플리케이션 이름` hellorest
   2. `Kubernetes 클러스터` autopilot-cluster-1 (asia-northeast3)
3. `배포`

몇분 기다리면 배포 결과 화면이 나온다. 리소스가 모자른다는 식의 에러들은 무시하자. 리프레시 하다보면 에러가 없어진다. 배포작업 완료후에 측정해야할 값들을 배포중에 측정함으로써 리소스가 모자라다고 출력된가 아닐까? 라고 추측은 해보지만 뇌피셜이다.

<details> <summary> 배포된 상태의 클러스터 </summary>

![deploy](.resources/gcp_k8s_engine_autopilot/cluster_overview_01.png)
![deploy](.resources/gcp_k8s_engine_autopilot/cluster_overview_02.png)

</details>

설명

### 서비스 노출

서비스 노출 하라고 나올 것이다. 이 클러스터는 진입점이 없이 pod 만 배포된 상태이다. 이를 설정해야 접속이 가능하다.

- `포트매핑`
  - `포트` 80
  - `대상 포트` 8080
  - `프로토콜` TCP
- `서비스 유형` 부하 분산기
- `서비스 이름` hellorest-service

다음과 같은 yaml로 실행된다고 한다.

```yaml
---
apiVersion: "v1"
kind: "Service"
metadata:
  name: "hellorest-service"
  namespace: "default"
  labels:
    app: "hellorest"
spec:
  ports:
  - protocol: "TCP"
    port: 80
    targetPort: 8080
  selector:
    app: "hellorest"
  type: "LoadBalancer"
  loadBalancerIP: ""
```

![service_detail](.resources/gcp_k8s_engine_autopilot/service_detail.png)

접속 가능하다.

```bash
% curl http://34.64.200.39/hello
GitHub ver 2. I am hellorest-59d4598d88-fd4w9(10.114.0.194)
```

### 알게 모르게 생성된 infra

생성되는 것
Kubernetes 요소들, Kubernetes node health check
네트웍 요소들: Load Balancer, 외부IP, 포워딩 룰

#### VM Instance

VM 인스턴스는 생성되지 않는다.

```bash
% gcloud compute instances list
Listed 0 items.
```

반면 autopilot 모드가 아닌경우 다음과 node instance 가 생성된다.

![create node](.resources/gcp_k8s_engine_autopilot/created_cluster_k8s.png)

생성 정보
이름: k8s-cluster-1
리전: asia-northeast3

-- 일반 클러스터의 스크린샷 --------------------------

#### 외부 IP

메뉴: VPC 네트워크 > 외부 IP 주소

![exposed_ip](.resources/gcp_k8s_engine_autopilot/created_cluster_k8s.png)

노드 부분은 실제 인스턴스이다. 실제 이는 vm instance 에서도 확인 가능하다.

![exposed_ip](.resources/gcp_k8s_engine_autopilot/create_k8s_instances.png)

#### LoadBalancer & IP

메뉴: 네트워크 서비스 > 부하 분산

![exposed_ip](.resources/gcp_k8s_engine_autopilot/load_balancer_detail.png)

## Kubernetes (k8s)

어떤가 autopilot 모드로 region 단위 가용성을 가지는 http rest 서비스를 만드는 것은 매우 쉽지 않은가?

---

## 이제 우리가 한 일을 설명해보자

### Kubernetes?

Kubernetes란 무엇인가? 원문: [쿠버네티스란 무엇인가? - kubernetes.io](https://kubernetes.io/ko/docs/concepts/overview/what-is-kubernetes/)

아래그림은 container가 무엇인지를 기존 인프라와 비교하여 설명하고 있다.

![container evolution](.resources/gcp_k8s_engine_autopilot/container_evolution.svg)

쿠버네티스는 다음을 제공한다.

- `서비스 디스커버리와 로드 밸런싱` 쿠버네티스는 DNS 이름을 사용하거나 자체 IP 주소를 사용하여 컨테이너를 노출할 수 있다. 컨테이너에 대한 트래픽이 많으면, 쿠버네티스는 네트워크 트래픽을 로드밸런싱하고 배포하여 배포가 안정적으로 이루어질 수 있다.
- `스토리지 오케스트레이션` 쿠버네티스를 사용하면 로컬 저장소, 공용 클라우드 공급자 등과 같이 원하는 저장소 시스템을 자동으로 탑재 할 수 있다.
- `자동화된 롤아웃과 롤백` 쿠버네티스를 사용하여 배포된 컨테이너의 원하는 상태를 서술할 수 있으며 현재 상태를 원하는 상태로 설정한 속도에 따라 변경할 수 있다. 예를 들어 쿠버네티스를 자동화해서 배포용 새 컨테이너를 만들고, 기존 컨테이너를 제거하고, 모든 리소스를 새 컨테이너에 적용할 수 있다.
- `자동화된 빈 패킹(bin packing)` 컨테이너화된 작업을 실행하는데 사용할 수 있는 쿠버네티스 클러스터 노드를 제공한다. 각 컨테이너가 필요로 하는 CPU와 메모리(RAM)를 쿠버네티스에게 지시한다. 쿠버네티스는 컨테이너를 노드에 맞추어서 리소스를 가장 잘 사용할 수 있도록 해준다.
- `자동화된 복구(self-healing)` 쿠버네티스는 실패한 컨테이너를 다시 시작하고, 컨테이너를 교체하며, '사용자 정의 상태 검사'에 응답하지 않는 컨테이너를 죽이고, 서비스 준비가 끝날 때까지 그러한 과정을 클라이언트에 보여주지 않는다.
- `시크릿과 구성 관리` 쿠버네티스를 사용하면 암호, OAuth 토큰 및 SSH 키와 같은 중요한 정보를 저장하고 관리 할 수 있다. 컨테이너 이미지를 재구성하지 않고 스택 구성에 시크릿을 노출하지 않고도 시크릿 및 애플리케이션 구성을 배포 및 업데이트 할 수 있다

Kubernetes components 출처: [쿠버네티스 컴포넌트 - kubernetes.io](https://kubernetes.io/ko/docs/concepts/overview/components/)

![components of kubernetes](.resources/gcp_k8s_engine_autopilot/components-of-kubernetes.svg)

각 components 에 대한 상세 내용은 링크 안에서!

### Kubernetes Cluster

위에서는 k8s의 구조를 살펴봤다. 그렇다면 이것이 cloud 상에서 배포되었을때 어떤 형태인지 살펴보자. 다음은 azure aks 마이크로서비스 아키텍처 문서이다. 위 gke autopilot hands-on 의 결과로 생성된 클러스터는 aks microservice architecture의 일부이다.

[AKS(Azure Kubernetes Service)의 마이크로 서비스 아키텍처](https://docs.microsoft.com/ko-kr/azure/architecture/reference-architectures/containers/aks-microservices/aks-microservices)

![aks microservice architecture](.resources/gcp_k8s_engine_autopilot/aks_microservice_architecture.png)
