# GRPC

이 문서는 다음 내용을 다룬다.

- GRPC 란?
- 기존 통신 방법과의 차이점
- 약간의 운영 지식

GRPC가 RPC의 일종이긴 하지만 이글에서는 기존의 RPC와 비교하기보다는 가장 흔히 사용되는 HTTP 1.1 + JSON 조합 즉 RESTful API와 비교한다. 사실 구글이 GRPC라고 이름을 붙이긴 했으나 기본적으로 통신패턴이 "procedure call" 은 아니기에 RESTful API와 비교하는 쪽을 택했다. 여기까지 읽으면 대충 짐작하겠지만 이글의 주된 내용은 GRPC가 특정 분야에서 RESTful API를 대체하는 기술이 될 수 있다는 것이다.

물론 RPC처럼 쓸수도 있겠지만 어쨌든 기술이 지원하는 기능으로 볼때 GRPC는 완벽한 작명은 아니라고 생각한다.

뭐 그냥 그렇다고... 구글하고 내가 소스리뷰하는 관계는 아니니까. 

![](resources/grpc/grpc-usage.png)
https://docs.microsoft.com/en-us/dotnet/architecture/cloud-native/rest-grpc

# 배경지식

GRPC는 Remote Procedure Call(이하 RPC)의 하나로써 payload로 Google Protocol Buffers(이하 protobuf)로 serialize된 데이터를 사용한다. 들어가기 전에 RPC와 protobuf에 대해서 알아보자.

RPC는 매우 오래된 개념으로 여러 종류의 rpc 구현체가 있다.

- Sun RPC
- XML-RPC

RPC에 대해서는 자세히 살펴보지는 않는다.

## Google Protocol Buffers

Protocol Buffers이 무엇인지 살펴보자.

[Protcol Buffers](grpc_protocol_buffers.md)

# GRPC

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

프로토콜 버퍼의 단점을 답습한다.
protobuf가 human readable 하지 않다.
Schema가 존재한다는 것이 단점이 될 수 있다. json의 경우 하위호환을 유지한채로 서버를 패치가능. Protobuf는 deserialize 실패로 인해 힘들다. 해결 가능한지 확인 필요


## 운영 지식

### 업데이트

RESTful 은 스위치에서 서버로 추가 requesg를 보내지 않고, 아사를 기다린다. (스텝바이스텝으로 설명)
반면 connection이 있기 때문에 상대 peer가 떠 있으면 자연스럽게 커넥션이 계속 연결되어 있다. Graceful 하게 통신을 종료하는 처리가 되어 있지 않으면 패킷 loss가 발생할 수 있다.

### Load balancing

앞에서도 설명했듯이 GRPC는 HTTP 2.0에 기반.
HTTP 2.0은 connection을 맺고 통신을 하는 형태이기 때문에 기존의 rest api 처럼 round-robin 방식으로 통신할 수 없다.
Least connection이 한가지 답이 될 수 있다. 하지만 이 역시 완벽한 답을 될 수 없다. 게임 서버를 가정해보자. 게임의 경우 사용자가 많은 월드가 있고 그렇지 않은 월드가 있다. 이경우 사용자가 많은 월드에서 더 많은 패킷을 보내게 되고, 그 커넥션은 더 많은 리소스를 사용하게 된다. 그로 인해 밸런싱이 제대로 이뤄지지 않게 된다.

### Akamia 연동

GRPC 에 연결되는 서버는 akamai로 들어오는 상황을 가정해보자.
Akamai의 경우 성능 측정을 위해 static url로 접근할 수 있는 페이지를 등록해야한다.
하지만 GRPC에서 사용하는 서버 (jetty)에서 이 기능을 지원해야하는데....
DSA IPA 일단 둘다 알아보고 글을 씁시다.

