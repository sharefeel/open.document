# Protocol Buffers

프로토콜 버퍼가 뭐냐? 이 자료는 구글에서 베끼자

# 해보자

Java 에서 해보자. 기본적으로는 공식홈페이지의 튜토리얼 중 [Basic:Java](https://developers.google.com/protocol-buffers/docs/javatutorial) 부분을 참고했으며 약간의 살을 더했다.

돈되는 product가 아니라 그런지 (2020년기준) 나온지 11년이 지나도록 문서의 한글화를 안해준다. 개인적으로는 2013년에 이 문서를 처음 접했는데 그 뒤로 내용이 바뀐 것도 없다. 심지어 protobuf 버전3이 나왔지만 이 문서는 버전2이다.

## 준비작업

### protoc 설치
protoc는 .proto 파일을 언어별 클래스를 생성하는 컴파일러이다.

[Github protobuffers release](https://github.com/protocolbuffers/protobuf/releases)에 접속하면 필요한 컴파일러를 다운받을 수 있다. 컴파일러는 압축 파일의 이름에 따라서 다음과 같이 분류되며 필요한 버전을 받아서 압축을 풀자.
- `protobuf-언어-*` 컴파일러의 언어별 빌드 소스
- `protoc-버전-플랫폼-아키텍쳐-*` 플랫폼과 아키텍처 별 prebuild된 컴파일러 바이너리

기본적으로 protobuf 컴파일러는 c++ 로 개발되어 있으며 소스코드 버전은 configure와 make 기반으로 빌드가 필요하다. 이 글에서는 prebuild된 컴파일러 기준으로 설명한다. (압축을 푼 디렉토리는 $PROTO_HOME으로 가정)

## 스키마(타입) 정의 및 Java 클래스 변환 (컴파일)

....

### .proto 작성

다음은 전화부(AddressBook)의 스키마(데이터 타입)를 정의하는 .proto 파일이다. 

```
// code came from https://developers.google.com/protocol-buffers/docs/javatutorial
syntax = "proto2";

package net.youngrok.gist.protos;

option java_package = "net.youngrok.gist.protos";
option java_outer_classname = "AddressBookMessage";

message Person {
    required string name = 1;
    required int32 id = 2;
    optional string email = 3;

    enum PhoneType {
        MOBILE = 0;
        HOME = 1;
        WORK = 2;
    }

    message PhoneNumber {
        required string number = 1;
        optional PhoneType type = 2 [default = HOME];
    }

    repeated PhoneNumber phones = 4;
}

message AddressBook {
    repeated Person people = 1;
}
```
서두에 언급했듯이 이 .proto 정의 version 2 이다. 실제 언어 스펙은 아래 링크를 참고
- `language guide v2` https://developers.google.com/protocol-buffers/docs/proto
- `language guide v3` https://developers.google.com/protocol-buffers/docs/proto3

위 protobuf 파일을 기반으로 json 샘플을 작성하면 대충 이런 식을 것이다.

```
{
    "people": [
        {
            "name": "rock",
            "id" : 32,
            "email": "rock@nroll.com",
            "phones": [
                {
                    "number": "010-1024-2048",
                    "type": "MOBILE"
                },
                {
                    "number": "02-3273-8783",
                }
            ]
        },
        {
            "name": "kai",
            "id" : 33,
            "email": "kai@database.org",
            "phones": [
                {
                    "number": "010-1677-7216",
                    "type": "MOBILE"
                }
            ]
        }
    ]
}

```

### .proto 파일 컴파일

설치했던 protobuf 컴포일러로 .proto 파일을 컴파일하여 언어의 클래스를 생성한다. 기본적인 protoc 사용법은 다음과 같다.
```
$ protoc -I=$SRC_DIR --java_out=$DST_DIR $SRC_DIR/addressbook.proto
```
- `$SRC_DIR` .proto 파일 위치
- `$DST_DIR` 자바 소스 디렉토리
- `$SRC_DIR/addressbook.proto` 컴파일할

실행 예)
Java 프로젝트를 생성하고 resource/protos 디렉토리에 위 .proto 파일을 위치 시킨후 컴파일 해보자. protoc는 resource/protos 즉 .proto 파일이 저장된 디렉토리에서 실행했다

실행한 명령어
```
$ protoc -I=. --java_out=../../java ./addressbook.proto
```
결과

![](resources/grpc/compiled_protobuf_class.png)

person.proto에 정의된 대로 net.youngrok.gist.protos 패키지에 AddressBookMessage 클래스가 생성된 것을 볼 수 있다. 

### 라이브러리

위까지 실행하면 컴파일에러를 잔뜩 안고 있는 AddressBookMessage 클래스를 얻을 수 있다. 다른 IDL처럼 protobuf 역시 라이브러리가 필요하다. pom.xml 파일에 다음 의존성을 추가.
```
<!-- https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java -->
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>3.11.3</version>
</dependency>
```

### Maven plugin

protoc를 직접 사용해서 컴파일할 수도 있겠지만 좀더 편하게 maven의 generate-sources 단계에서 클래스를 생성하도록 설정해보자. Maven 플러그인을 통해서 할 수 있는데 두가지 방법이 대표적이지 않을까 한다.

#### 방법1 maven-ant-plugin



#### 방법2 protobuf-maven-plugin


## 사용




# 타 IDL과의 비교

## JSON / XML
이 글을 쓴 목적이지 사실

- 스키마 있음 --> 오류가 적음
- 바이너리
  - 빠름 (serialize/deserialize >> json parsing
  - human readable 하지 않음
  - POJO 형태의 access / json object는 map기반 or pojo로 쓰기위해서는 object mapping 필요 (에러시 어떻게 되나?)
- 작은사이즈 --> 전송량 감소, 메모리사용량감소

## Avro


## Thrift


### IDE 플러그인
IntelliJ 를 사용한다면 [IntelliJ Protobuf Support plugin](https://plugins.jetbrains.com/plugin/8277-protobuf-support)을 설치하자. Syntax validation, syntax highlighting, code formatting 등의 개발 편의기능을 제공한다. 아래는 실제 적용한 예이며 formatting도 플러그인의 도움을 받았다.

![](resources/grpc/protobuf_plugin_screenshot.png)


# 참고

Protocol Buffers 공홈: https://developers.google.com/protocol-buffers

Java 프로그램 가이드: https://developers.google.com/protocol-buffers/docs/javatutorial

프로토콜 버퍼 컴파일러: https://github.com/protocolbuffers/protobuf/releases/tag/v3.11.3

Maven protocol buffers plugin: https://www.xolstice.org/protobuf-maven-plugin/examples/protoc-artifact.html

IntelliJ protobuf 플러그인: https://plugins.jetbrains.com/plugin/8277-protobuf-support
