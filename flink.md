# Flink

## 필자 수준

글 작성자가 가진 flink 에 대한 경험은 샘플 몇개 실행하고서 UI 구경해본게 전부이다. 하지만 hot 하다고들 하니 일단 배경 지식부터 좀 쌓고, 어떤 use case 하나 가정해서 코딩해보는 기로 했다. 돌이켜보면 storm이든 spark이든 처음에 워드카운트 돌려보고서 알 수 있는 건 아무것도 없고 일단 어느정도 개념을 쌓아야 했다. 그나저나 왜 워드카운트를 가장 먼저 샘플로 내세우는 걸까?

## 어떻게 접근할까?

어떤 식으로 flink 에 접근할지 고민이 됐다.

0. Akka 에 기반하고 있다고 하니 actors 패턴부터 시작해서 플랫폼 내부에 대해서 다룰까? [Akka and Actors](https://cwiki.apache.org/confluence/display/FLINK/Akka+and+Actors)
1. 스트림 프로세싱에 대한 배결설명부터 설명할까?
2. 대표적인 use case 를 잡고 설명할까?
3. 다 됐고 쉬운 것으로 hands on을 할까?

사실 마음같아서는 다하고 싶지만 막상 하나도 제대로 작성할 능력이 안되는 게 현실이다. 그래서 비교적 쉬운 방식인 (하지만 가장 지루한 방식인) 1번으로 하기로 했다.

필자가 너무 무식한 사람으로 깔고 시작했는데 그래도 아주 오래전에 storm, kafka 등의 스트림 프로세싱 툴을 운영하고 코딩도 해봤다.

## 배경 지식

### 배치 프로세싱 vs 스트림 프로세싱

[아파치 실시간 처리 프레임워크 비교분석 (1) - popit.kr](https://www.popit.kr/%EC%95%84%ED%8C%8C%EC%B9%98-%EC%8B%A4%EC%8B%9C%EA%B0%84-%EC%B2%98%EB%A6%AC-%ED%94%84%EB%A0%88%EC%9E%84%EC%9B%8C%ED%81%AC-%EB%B9%84%EA%B5%90%EB%B6%84%EC%84%9D-1/)

[아파치 실시간 처리 프레임워크 비교분석 (2) - popit.kr](https://www.popit.kr/%EC%95%84%ED%8C%8C%EC%B9%98-%EC%8B%A4%EC%8B%9C%EA%B0%84-%EC%B2%98%EB%A6%AC-%ED%94%84%EB%A0%88%EC%9E%84%EC%9B%8C%ED%81%AC-%EB%B9%84%EA%B5%90%EB%B6%84%EC%84%9D-2/)


### 스트림 프로세싱

### 스트림 프로세싱 플랫폼 비교



## Flink

### 구조

### 프로그래밍 모델

### 간단한 모델

## GCP 로 해보자

### 가입

최초 사용시 90일간 300$을 자유롭게 사용할 수 있다. 카드정보를 입력하지만 자동 결제가 되지 않는다는 것을 매우 강조하고 있다. 유치한 디스로 보일 수 있다. (하지만 나역시 50만원쯤 날려먹은 기억이 있어서 유치하다고 말하기엔 자괴감이 좀 든다.)

다음 화면이 나올때까지 쭉 입력하자.

![GCP 가입](.resources/gcp_streaming_processing/gcp_register.png)


우선 참고 문서. 백서이긴 하지만 내용은 읽기 불편하지 않다.

[Google Streaming Analytics Platform White Paper](https://services.google.com/fh/files/misc/google-streaming-analytics-platform.pdf)



