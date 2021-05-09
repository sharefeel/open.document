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

<<<<<<< HEAD
Cloud Build가 아닌 management VM에서 kubectl을 하도록 한다. 그 장비는 private ip인 endpoint에 접속 가능해야 한다. 그리고 cloud build는 gcloud를 통해서 해당 장비에가 kubectl을 실행하게 한다.

사실 management vm의 경우 cloud build의 proxy역할도 하지만 k8s 클러스터 관리를 위해서 어차피 생성해야 한다.
=======
Compute Engine을 gke 클러스터와 같은 서브넷 내에 생성한다. 이 VM은 private endpoint에도 kubectl 명령어를 실행할 수 있고, node와 pod에도 접속가능하다.
Cloud Build에서는 이 VM에 gcloud ssh를 통해서 kubectl 명령어를 실행한다.

사실 CloudSDK를 사용해서는 k8s 관리가 충분하지 않기 때문에 어차피 클러스터를 관리할 VM이필요하다. Cloud SDK
>>>>>>> f26487f82a03fc73830566d42d543cba3b7e15dd

## 해보자

### 준비작업

#### Network

VPC, Subnet 생성

#### GKE Cluster

1. Cluster
3. Pod 배포

#### 배포할 소스코드 github



### 배포를 실패해보자

#### 트리거 작성

#### 배포 실패

봐라.. 에러 나지?

External ip가 있으면 성공한다.

### 그러면 성공하도록 고쳐보자

핵심은 GKE Manager 에서 실행되도록 하는 것이다.

같은 subnet 에 gke manager 를 만들자.

CloudBuild 에서 kubectl 을 gcloud 로 감싸서 실행한다.

#### 배포 성공

어때 되지 않냐?

