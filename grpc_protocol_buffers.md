# Protocol Buffers

## 해보자

### protoc 설치
protoc는 .proto 파일을 언어별 클래스를 생성하는 컴파일러이다.

[Github protobuffers release](https://github.com/protocolbuffers/protobuf/releases)에 접속하면 필요한 컴파일러를 다운받을 수 있다. 컴파일러는 압축 파일의 이름에 따라서 다음과 같이 분류되며 필요한 버전을 받아서 압축을 풀자.
- `protobuf-언어-*` 컴파일러의 언어별 빌드 소스
- `protoc-버전-플랫폼-아키텍쳐-*` 플랫폼과 아키텍처 별 prebuild된 컴파일러 바이너리

기본적으로 protobuf 컴파일러는 c++ 로 개발되어 있으며 소스코드 버전은 configure와 make 기반으로 빌드가 필요하다. 이 글에서는 prebuild된 컴파일러 기준으로 설명한다. (압축을 푼 디렉토리는 $PROTO_HOME으로 가정)

### IDE 플러그인
IntelliJ 를 사용한다면 [IntelliJ Protobuf Support plugin](https://plugins.jetbrains.com/plugin/8277-protobuf-support)을 설치하자.





# 참고

Protocol Buffers 공홈: https://developers.google.com/protocol-buffers

Java 프로그램 가이드: https://developers.google.com/protocol-buffers/docs/javatutorial

프로토콜 버퍼 컴파일러: https://github.com/protocolbuffers/protobuf/releases/tag/v3.11.3

Maven protocol buffers plugin: https://www.xolstice.org/protobuf-maven-plugin/examples/protoc-artifact.html

IntelliJ protobuf 플러그인: https://plugins.jetbrains.com/plugin/8277-protobuf-support
