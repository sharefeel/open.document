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

### 설정파일: airflow.cfg

$AIRFLOW_HOME/airflow.cfg 파일이 위치해야 한다. 설치된 파일중 템플릿 설정 파일을 복사한 후 수정하면 된다.

```bash
# MacOS의 경우 python3 site-packages 하위에 airflow 파일들이 설치된다.
ls /usr/local/lib/python3.7/site-packages/apache_airflow-2.0.0.dev0-py3.7.egg
# 위 경로에서 설정 템플릿 파일을 복사하자
cp /usr/local/lib/python3.7/site-packages/apache_airflow-2.0.0.dev0-py3.7.egg/airflow/config_templates/default_airflow.cfg $AIRFLOW_HOME/
cd $AIRFLOW_HOME
mv default_airflow.cfg airflow.cfg
```

사실상 아무런 설정도 하지 않고 그대로 사용할 수 있다. 그러면 다음 설정으로 동작하게 된다.
- `webserver`0.0.0.0:8080
- `db` sqlite, 파일위치는 $AIRFLOW_HOME/airflow.db
- `dag 경로` $AIRFLOW_HOME/dags

기본적인 설정 수정은 다음 정도일 것이다.
```bash
# Airflow 가 DAG을 읽어들일 경로이다. 즉 사용자는 DAG을 작성하여 이 경로에 복사하면 되다.
# 절대경로여야 한다.
dags_folder = {AIRFLOW_HOME}/dags

# 데이터를 저장한 데이터베이스로 아래는 sqlite 의 기본 설정
sql_alchemy_conn = sqlite:///{AIRFLOW_HOME}/airflow.db
# mysql, postgreql 등의 DBMS도 지원하며 관리차원에서는 DBMS를 선택하는 것이 낫다. 아래는 mysql 예시
sql_alchemy_conn = mysql:///user:password@mysqlhost:3306/airflow.db

# webserver uri는 아래 세 설정을 사용하면 돈다.
base_url = http://localhost:8080
web_server_host = 0.0.0.0
web_server_port = 8080

# LDAP 지원이 추가되었는데 해보지 않았다. 관심있으면 try 해보길..
[ldap]
uri = 
user_filter = ..
:

```

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

## 타임존
Airflow의 시간은 기본적으로 UTC 기반으로 동작하는데 이게 한국에 사는 우리 입장에선 매우 짜증난다. 표면적으로 시간으로 짜증나는 경우는 세가지이다.
1. Dashboard UI의 표기 시간
2. UI에 표기되는 DAG실행 시간
2. DAG 코딩시에 시간 파라미터

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

