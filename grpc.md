# GRPC

## 들어가기 전에

GRPC 문서는 [grpc.io](http://grpc.io)에 매우 잘 설명되어 있다. 스펙이나 사용문법 나열에 그치지않고 현업에 바로 적용가능한 설명들이 있으니 이미 GRPC를 써야겠다고 결정한 사람은 이 글은 skip하고 바로 grpc.io로 가시라. 아직 마음을 못정했거나 많은 영문 글을 읽기 싫은 사람에게는 의미가 있을 수 있겠다.

## 글의 내용

- GRPC에 대한 설명과 이해를 위한 배경지식
- HTTP 1.1/JSON과의 차이점
- 운영 이슈

Google RPC 라고 이름이 지어져 있지만 구글 스스로도 기존의 rpc(sun rpc, xml rpc) 등이 아닌 RESTful API(http 1.1/JSON)을 타겟으로 하여 비교하고 있다. 실제 GRPC가 RESTful API와 비교할때 장점이 두드러지고 우수한 대체제이기 때문에 포커스를 그쪽으로 맞추는 것이 당연하다.

# 배경지식

GRPC는 Remote Procedure Call(이하 RPC)의 하나로써 payload로 Google Protocol Buffers(이하 protobuf)로 serialize된 데이터를 사용한다. 들어가기 전에 RPC와 protobuf에 대해서 알아보자.

## Remote Procedure Call (RPC)

RPC는 매우 오래된 개념으로 여러 종류의 rpc 구현체가 있다.

- Sun RPC
- XML-RPC

RPC에 대해서는 자세히 살펴보지는 않는다.

https://redcoder.tistory.com/126

https://www.geeksforgeeks.org/remote-procedure-call-rpc-in-operating-system/

## Google Protocol Buffers (Protobuf)

GRPC는protobuf로 serialize된 데이터를 메세지의 payload에 사용한다. 그러니 우선 protobuf이 무엇인지 살펴보자.

[Protcol Buffers](grpc_protocol_buffers.md)

# GRPC란?

## RESTful과 GRPC의 통신 방식 비교

HTTP 1.1 / ... 표 형태로

통신의 peer에 따른 특징
- Client to Server
- Server to Server
RESTful API는 둘 모두 사용가능하다.
HTTP라는 범용적인 프로토콜을 사용하고 있고 json 라이브러리는 웹에 사용되는 모든 언어에 존재한다. 없다면 그 언어는 쓰지마라. 웹용이 아니다.


## 서버 사이드

### 서버 통신에서 restful의 단점
HTTP의 단점
모든 커넥션

JSON의 단점
큰 프로토콜 사이즈
schema-less
구문 오류

### GRPC가 이것을 해결한다. 
GRPC가 이 문제들을 해결한다.
굳

## GRPC 단점

GRPC는 훌륭하고 완성도 높은 (한편으론 특별할 것 없는) 기술이다. 이런 기술들은 이미 **문제점**은 거의 없어진 상태이며 최종적으로는 기술이 주는 장점을 취하기 위해 심각하지 않은 수준의 **해결 불가능한 단점**을 용인하면서 사용한다. 단점을 완화하든 회피하든 그런식으로 말이다.

<figure align="middle">
  <img src="resources/grpc/fork_spoon.png" title="하나만 쓸수 있다면?"/>
  <figcaption><b>하나만 쓸수 있다면?</b></figcaption>
</figure>

Trade-off로 감안할 수 밖에 없는 불가능한 단점은 GRPC의 기반기술에서 온다.

### RPC 의 한계
### Protobuf를 사용으로 인한 단점 
### HTTP 2.0을 사용한다는 단점
Http 2.0은 1.1 대비 많은 장점을 제공한다. 하지만 connection oriented 프로토콜이라는 점에서 몇몇 단점이 존재하며 이는 

# 적용

## Micro Service Architecture 에서의 사용

https://medium.com/@goinhacker/microservices-with-grpc-d504133d191d

https://levelup.gitconnected.com/grpc-in-microservices-5887caef195

![](resources/grpc/grpc-usage.png)

https://docs.microsoft.com/en-us/dotnet/architecture/cloud-native/rest-grpc




# 운영 지식

## HTTP1.1 API 방식과의 차이

HTTP 1.1의 기반 API의 호출 절차이다.
1. connect - request 1 - close
2. connect - request 2 - close
3. :

반면 HTTP 2.0은 다음과 같다.
1. connect
2. request 1
3. request 2
4. :
5. close

Caller수 / callee수 / tps 등에 상관없이 sticky session 방식만 아니라면 비교적 균일하게 callee의  

HTTP 2.0은 connection을 맺고 통신을 하는 형태이기 때문에 기존의 rest api 처럼 round-robin 방식으로 통신할 수 없다.
Least connection이 한가지 답이 될 수 있다. 하지만 이 역시 완벽한 답을 될 수 없다. 게임 서버를 가정해보자. 게임의 경우 사용자가 많은 월드가 있고 그렇지 않은 월드가 있다. 이경우 사용자가 많은 월드에서 더 많은 패킷을 보내게 되고, 그 커넥션은 더 많은 리소스를 사용하게 된다. 그로 인해 밸런싱이 제대로 이뤄지지 않게 된다.


### Load balancing 메카니즘의 변화

https://grpc.io/blog/loadbalancing/


### Load balancing (linkerd)

Kubernetes linked 관련 설명

https://kubernetes.io/blog/2018/11/07/grpc-load-balancing-on-kubernetes-without-tears/


### Rolling update

RESTful 은 스위치에서 서버로 추가 request를 보내지 않고, 아사를 기다린다. (스텝바이스텝으로 설명)
반면 connection이 있기 때문에 상대 peer가 떠 있으면 자연스럽게 커넥션이 계속 연결되어 있다. Graceful 하게 통신을 종료하는 처리가 되어 있지 않으면 패킷 loss가 발생할 수 있다.



### Akamia 연동

GRPC 에 연결되는 서버는 akamai로 들어오는 상황을 가정해보자.
Akamai의 경우 성능 측정을 위해 static url로 접근할 수 있는 페이지를 등록해야한다.
하지만 GRPC에서 사용하는 서버 (jetty)에서 이 기능을 지원해야하는데....
DSA IPA 일단 둘다 알아보고 글을 씁시다.

