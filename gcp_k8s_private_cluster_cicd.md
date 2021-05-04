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

이때 control plane의 endpoint가 external ip를 가지지 않은 경우 4.2 단계에서 실패하게 된다. 