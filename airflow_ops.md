# 개요
이 문서에서는 apache airflow를 설치하고 운영하는 방법을 다룬다. 사실 airflow 운영은 

# Install & run

이 문서는 맥북 프로 2016, macOS Mojave, python3, Airflow 1.10.9 기준으로 작성되었다. Airflow 는 PIP와 소스설치 두가지 방법으로 설치가능하다.

## Install

### 준비

아래 내용은 알아서들 설치하시고. Airflow는 python3로 구동되며 DAG 역시 python으로 작성한다. 하지만 기존에 python을 사용해보지 않았다고 해도 DAG을 작성하는 것은 문제가 없다. 설치하면 기본적으로 example DAG이 있기 때문에 수정을 해서 사용하면 된다. 만약 자신만의 operator를 작성하려면 airflow 내부 동작과 python의 클래스와 상속 방법은 알고 있어야 한다. Operator는 이후 다시 설명한다.

- `python3`
- `pip` pip로 설치할 경우에만 필요
- `DB` 데이터 저장용 DB로 mysql 추천. 별도 설치하지 않으면 sqlite가 사용됨.

### PIP Install

pip 기반 설치는 매우 쉽다. pip만 제대로 설치되어 있다면 말이다.
```bash
$ pip install
```

### 소스 기반 설치

github 에서 1.10.9 소스 코드를 다운 받아서 설치한다.
```bash
$ git clone https://github.com/apache/airflow.git
Cloning into 'airflow'...
$ cd airflow
$ python setup.py install
:
Finished processing dependencies for apache-airflow==2.0.0.dev0
```

## 설정

### AIRFLOW_HOME

일단 메뉴얼에서는 AIRFLOW_HOME을 ~/airflow 로 가이드하고 있다. AIRFLOW_HOME 하위에는 

### 설정 파일 편집

airflow.cfg 이야기

## 실행

### DB 초기화 및 필요한 프로세스 실행

```bash
# 데이터베이스 초기화 작업이 필요하다. 이 작업은 한번만 해야 한다.
$ airflow initdb
# Front-end 역할을 하는 웹서버 실행. Foreground로 실행된다. Daemon으로 실행하는 것은 각자 알아서.
$ airflow webserver
# 실제 dag을 실행하는 스케줄러 프로세스를 실행
$ airflow scheduler
````

### 짠~ UI 가 떴습니다.

예제 DAG 들이 있습니다.

# DAG 작성

Airflow 를 쓴다는 것은 airflow를 운영하는 것과 dag을 작성하여 workflow를 관리하는 것으로 나눠집니다. DAG 작성은 


# 기타 운영 이슈

## SPOF
Airflow는 일반적으로 시스템 내에서 매우 중요한 위치를 차지하지만 문제는 그 중요도에 비해서 가용성 부분에서 취약하다. 

`webserver` DAG 코드는 파이썬 파일로저장되며 동작중인 상태는 데이터베이스에 저장되며 웹서버는 stateless 하고 동작하는 front-end이다. 따라서 프로세스가 다운되더라도 단순히 재실행하기만 하면 된다.
`scheduler` 실제 dag과 task의 실행을 담당하고 상태와 결과를 DB에 저장한다. 따라서 scheduler 프로세스가 죽는 것은 매우 심각한 상황이다.

## 자원 점수

Airflow 운영중 가장 큰 문제는 airflow의 자원 점유이다.

`Thread 점유` Airflow 각 task는 thread로 동작한다.
`메모리 부족` Airflow는 동시에 실행되고 있는 task의 수만큼 메모리를 필요로 한다.

Airflow 자원점유의 특징은 DAG이나 task의 총량이 아니라 `동시에 동작 중인 태스크 수`에 선형적으로 비례한다는 점이다. 이로 인해 발생하는 가장 심각한 문제는 **메모리가 부족한 경우 airflow 프로세스가 사라진다**는 점이다.

## 로그의 누적

## UI 반응성

Airflow front-end의 경우 많은 양의 정보를 보여준다. 특히 graph-view 

## ;aldjfdsj

