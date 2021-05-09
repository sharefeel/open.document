# Google Kubernetes Engine Private Cluster Container Deployment with CloudBuild

이 문서는 GKE Private cluster에 Cloud Build를 통해서 container를 배포하는 방법을 소개한다.

## 문제가 무엇인가?

GKE 클러스터에 보안상의 이유로 직접적인 접근을 제한하기 위해서 클러스터 생성시 다음 옵션을 줄 수 있다.

1. Private 클러스터로 구성
2. Control plane의 endpoint를 private ip로 지정

1번 설정에 의해 node는 external ip를 가지지 않게 되는데 이는 당연한 설정이고 이 문서의 관심사는 아니다. 이 문서에서 다루려는 것은 바로 2번으로 인해서 발생하는 컨테이너 배포 문제와 그 해결 방법이다. Cloud Build 를 통해 컨테이너를 배포하기 위해서는 다음 역할을 하는 트리거를 작성해야 한다.

1. 소스 저장소 clone
2. docker 빌드
3. Container Registry 로 빌드 결과 push
4. kubectl을 통해서 배포
   1. k8s 클러스터의 credential을 가져옴
   2. kubectl set image 를 통해서 pod의 컨테이너 이미지 교체

이때 control plane의 endpoint가 external ip를 가지지 않은 경우 4.2 단계에서 실패하게 된다. 이는 Cloud Build의 워커장비 즉 kubectl을 호출하는 장비가 endpoint에 접속을 못하기 때문이다. 또 external ip를 부여한 후 방화벽으로 Cloud Build 의 접속만을 허용하는 방법도 사용할 수 없다. 이유는 Cloud Build 워커장비의 IP를 특정할 수 없기 때문이다.

## 어떻게 해결할 것인가?

Compute Engine을 gke 클러스터와 같은 서브넷 내에 생성한다. 이 VM은 private endpoint에도 kubectl 명령어를 실행할 수 있고, node와 pod에도 접속가능하다.
Cloud Build에서는 이 VM에 gcloud ssh를 통해서 kubectl 명령어를 실행한다.

사실 CloudSDK를 사용해서는 k8s 관리가 충분하지 않기 때문에 어차피 클러스터를 관리할 가상머신이 필요하다. 배포를 위해 새로운 무언가를 만들어내는 것은 아니며 존재하는 VM을 활용하는 것이다.

## 해보자

다음 순서로 할 것이다.

1. 준비, 세팅
   1. VPC, Subnet 설정
   2. 배포할 컨테이너 소스코드
   3. Pod 생성 방법
2. External Endpoint
   1. GKE Cluster 생성
   2. 트리거
   3. 배포 성공
3. Private Endpoint
   1. GKE Cluster 생성
   2. 배포 실패 (트리거 수정 없음)
4. Private Endpoint + Manager VM
   1. Manager VM 추가
   2. 트리거 수정
   3. 배포 성공

### 준비, 세팅

### External Endpoint Cluster

그냥 참고용이다.

### Private Endpoint

Worker에서 kubectl 실행은 실패한다.

### Private Endpoint + Manager VM

CloudSDK를 통해서 manager vm에서 kubectl을 실행하도록 한다.
