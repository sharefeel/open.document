# 개발자의 소통 도구 : Swagger, Lombok, JPA

블라블라
개발자는 다른 개발자와 소통을 한다. 

용어정의

- `생산자` 소통을 위한 컨텐츠를 작성하는 사람
- `소비자` 작성된 컨텐츠를 받아 이해하는 사람

## 개발자의 소통

### 잘하는 소통

소통에 있어서 개발자가 추구하는 항목들

- `스피드` 소통을 하는데 필요한 시간.
- `명확함` 
- `정확함`
- `디테일` 자세함
- `가독(시)성` 
- `겉멋` 매우 중요하다. 개발자에게 있어서 매우 큰 동기부여가 된다.

### 고전적인 소통

소통을 하는 주된 이유중 하나는 바로 내가 작성한 소스코드의 동작을 다른 사람에게 이해시키기 위해서이다.
고전적이 방법으로는 이해의 매개체로 "문서"를 사용하는 방법이 있다.

0. 소스코드 작성자가 문서를 작성
1. 문서를 읽고 ...

많은 사람들은 알고 있다. 이 작업은 시간이 많이 걸리고, 생성한 문서를 아무도 안볼수도 있으며, 문서 본다고 이해할거란 보장도 없다. 결국 문서화는 시간낭비라는 인식이 강화되고 문서로써의 요구사항만을 만족시키는 문서들이 양산된다.

#### 여담. 망분리가 되어어 있다면

만약 소통하는 곳 즉 문서를 작성하는 곳과 설명할 것의 위치가 다르다면 어떨까? 소스코드 또는 그 동작을 문서에 첨부또는 복사 불가능하거나 또는 매우 불편하고 어느 범위까지 복사가 허용되는지에 대한 가이드도 모호하다면? 후략.

#### 여담. 그림 to Code

그렇다면 왜 문서를 작성할까? 그 본질은 소스코드자체보다는 문서가 더 이해하기 쉽다는데 있다. 설계를 하고 그 문서를 작성하면 소스코드가 생성되는 툴들이 존재한다. 한때의 유행이었는지 여전히 연구중인지는 알 수 없으나 아직 그런 세상이 오지는 않았다. 소스코드가 선행한다. 

### Code as a Doument

그렇다면 소스코드 자체로 소통하면 되지 않나? 이를 도와주는 툴들이 많이 있다. 오늘은 이 관점에서 swagger, lombok, jpa를 소개하려고 한다.

또한 소스코드 그 자체로도 가독성이 있어야 한다. 이는  


## 도구

Swagger, Lombok, JPA 로 알아본다.

### Swagger

API Document 에서 소비자들은 어떤 것을 기대할까?

![API Document에 가장 중요하게 생각하는 것](.resources/communication_of_developers/most-important-documentation-chart.png)

참고.

[10 Ways to create easy-to-use compelling API Documeent - swagger.io](https://swagger.io/blog/api-documentation/create-compelling-easy-to-use-api-documentation/)

[The State of API 2019 Report - smartbear.com](https://smartbear.com/resources/ebooks/the-state-of-api-2019-report/?utm_medium=content-text&utm_source=swagger-blog&utm_campaign=10-ways-api-documentation)

그렇다면 swagger가 만들어낸 문서를 보자.

[Swagger 생성 UI](https://petstore.swagger.io/?_ga=2.24643192.370818538.1592102595-498966297.1592102595#/)

### Lombok

### JPA

## 간단한 프로젝트

다음 역할을 하는 springboot 기반 api 를 개발한다.

0. 로그 수신해서 mysql 데이터베이스에 저장하는 api
1. 데이터베이스에 저장된 로그의 수를 리턴하는 api

사용하는 database 환경

- dbms: mysql
- endpoint: localhost:3306
- database: mydb

### API 사용법

![API 목록](.resources/communication_of_developers/swagger_apilist.png)

![모델](.resources/communication_of_developers/swagger_models.png)

![로그 전송 API](.resources/communication_of_developers/swagger_newlog.png)

다음 세 로그를 전송

```json
{
  "aaid": "AAID-1",
  "eventLog": "This is android event 1",
  "eventTimeEpoch": 100000,
  "market": "playstore",
  "user": "somebody"
}

{
  "aaid": "AAID-1",
  "eventLog": "This is android event 2",
  "eventTimeEpoch": 200000,
  "market": "playstore",
  "user": "somebody"
}

{
  "eventLog": "This is iphone event",
  "eventTimeEpoch": 300000,
  "idfa": "IDFA-1",
  "market": "appstore",
  "user": "nobody"
}
```

안드로이드 로그수를 조회해보자

![android log count](.resources/communication_of_developers/swagger_countandroid.png)

### Step.2 lombok을 통한 코드 가독성 향상

### Step.3 Database에서 데이터는 어떻게 가져가나?

프로젝트 개요

프로젝트는 다음 두가지 API를 제공한다.


로그는 json 으로 받는다.

```json
{
  "user": "이벤트를 발생한 사용자",
  "market": "appstore 또는 playstore",
  "idfa": "apple 광고 ID. market이 appstore인 경우 저장",
  "aaid": "google 광고 ID. market이 playstore인 경우 저장",
  "eventLog": "이벤트",
  "eventTimeEpoch": "이벤트 발생 시간의 epoch time. 초단위"
}
```

![생성된 applog 테이블](.resources/communication_of_developers/applog_table.png)

![데이터베이스 select 결과](.resources/communication_of_developers/database_select.png)

## Tags

`#Lombok` `#Swagger` `#Jpa` `#Swagger`
