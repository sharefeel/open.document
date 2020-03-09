# 개요

이 문서에서는 apache airflow를 설치과 간단한 사용법에 대해서 다룬다. Airflow는 Airbnb에서 만든 Workflow Management System 중 하나로써 비슷한 시스템으로는 spotify의 luigi가 있고, hadoop 에코시스템에서는 oozie와 azkaban 등이 있다. Airflow를 사용한다는 것은 다음 두가지로 나눠볼 수 있다.

1. 실행할 dag을 작성
2. dag의 실행 

아래에 다시 설명하겠지만 dag은 workflow 라고 이해하면 된다. 이글은 dag의 개발보다는 airflow 운영과 dag을 스케줄링하는 방법 대해서 다룬다.

## Terms

### DAG (Directed Acyclic Graph)

Dag은 airflow에서 workflow라는 의미로 통용되지만 사실 directed acyclic graph라는 자료구조의 줄임말이다. 직역하면 방향성이 있고 순환구조가 없는 그래프인데 tree 역시 dag의 일종이다. [DAG 알고리즘(국문)](https://steemit.com/dag/@cryptodreamers/dag-dag-directed-acyclic-graph)을 보면 지루하지 않게 dag의 정의와 알고리즘으로써 역할 그리고 bit coin 등에 활용되는 것을 볼 수 있다. 저정도로는 직성이 안풀린다면 [wikipedia](https://en.wikipedia.org/wiki/Directed_acyclic_graph)를 읽어보면 어떨까?

하지만 airflow의 이해를 위해서 dag이란 자료구조를 알 필요는 없으며 순환구조가 없는 즉 시작과 끝이 있는 작업의 흐름이라고 이해하면 된다. Airflow에서 dag은 task의 실행순서와 의존성을 기술하는 언어라고 할 수 있으며 실제 작성은 python으로 한다. 다음 그림은 실제 airflow의 간단한 dag의 예제이다.

<figure align="middle">
  <img src=".resources/airflow/simple_dag_image.png" width="250" title="Simple DAG"/>
</figure>

Dag에는 세개의 task (condition, dummy_task_1, dummy_task_2)가 있다. 이 dag이 airflow에 의해서 실행되면, condition task가 먼저 실행된 후 dummy_task_1과 dummy_task_2가 수행된다. Dummy_task_1이 실행될지 혹은 dummy_task_2가 실행될지 혹은 둘다 실행될지는 dag 파이썬 코드에서 결정한다.

### Task

Task는 dag을 구성하는 하나의 실행 단위이다. Dag은 task의 관계를 기술하는 역할을 하며 *task가 인스턴스화되어 실행됨으로써 실제 작업이 실행*된다.

### Operator

Airflow dag은 파이썬으로 작성되는데 operator는 각 task가 수행할 수 있는 작업의 정의이며 파이썬 클래스로 정의된다. Airflow 에서 많은 수의 operator를 기본 제공하고 있으며 전체 목록은 [공식홈페이지](http://airflow.apache.org/docs/stable/_api/airflow/operators/index.html)에서 확인가능하다. Airflow를 적용할 시스템에 따라 다르겠지만 다음 operator는 범용적으로 사용된다.

- `bash_operator` Bash 명령어를 실행하는 operator
- `python_operator` Python 함수 호출하는 operator. 코드를 삽입할 수 있다는 것 뿐만 아니라 dag의 환경변수 활용이 용이하다는 면에서 유용하다. 다만 task간의 관계도로 풀어야할 것을 코드에 구현하는 것은 지양하는 것이 좋다.
- `http_operator` Http 호출을 하는 operator
- `dummy_operator` 실제 수행하는 작업은 없는 이름 그대로 dummy operator. Task간의 관계를 논리적으로 설명하기 위한 목적으로 사용가능

각 operator는 내부적으로 BaseOperator를 상속받아서 만들어져 있는데 필요하다면 사용자가 operator를 추가로 구현할 수 있다. 예를 들어 SSH로 원격 명령어를 실행하는 task가 필요한 경우, bash_operator로 ssh command를 실행할 수 있지만 더 **`멋지게`** ssh_operator를 구현해서 처리하는 방법도 있다. 

### DAG 예제 소스 코드

됐고 그냥 dag 소스를 하나 보자. 위의 task 세개로 이루어진 dag의 소스코드이다. 파이썬 문법을 모르더라도 대충 감이 올 것이다.

<details>
<summary> DAG 소스 </summary>

```python
"""
Example DAG demonstrating the usage of BranchPythonOperator with depends_on_past=True, where tasks may be run
or skipped on alternating runs.
"""

from airflow.models import DAG
from airflow.operators.dummy_operator import DummyOperator
from airflow.operators.python_operator import BranchPythonOperator
from airflow.utils.dates import days_ago

args = {
    'owner': 'Airflow',
    'start_date': days_ago(2),
    'depends_on_past': True,
}

# BranchPython operator that depends on past
# and where tasks may run or be skipped on
# alternating runs
dag = DAG(
    dag_id='example_branch_dop_operator_v3',
    schedule_interval='*/1 * * * *',
    default_args=args,
    tags=['example']
)


def should_run(**kwargs):
    print('------------- exec dttm = {} and minute = {}'.
          format(kwargs['execution_date'], kwargs['execution_date'].minute))
    if kwargs['execution_date'].minute % 2 == 0:
        return "dummy_task_1"
    else:
        return "dummy_task_2"


cond = BranchPythonOperator(
    task_id='condition',
    provide_context=True,
    python_callable=should_run,
    dag=dag,
)

dummy_task_1 = DummyOperator(task_id='dummy_task_1', dag=dag)
dummy_task_2 = DummyOperator(task_id='dummy_task_2', dag=dag)
cond >> [dummy_task_1, dummy_task_2]
```

</details>

## Airflow Install

설치해보자. 이 문서는 맥북 프로 2016, macOS Mojave, python3, Airflow 1.10.9 기준으로 작성되었다. Airflow 는 PIP와 소스설치 두가지 방법으로 설치가능하다.

### 준비

아래 것들을 설치하자. python3만 필수이고 나머지 둘은 선택이다.

- `python3`
- `pip` pip로 설치할 경우에만 필요
- `DBMS` Airflow는 상태를 저장할 데이터베이스. Sqlite를 기본지원하지만 sqlite를 사용하면 동시에 여러개의 task가 실행되지 못한다. 이는 sqlite의 concurrent write가 제한적이기 때문이다. 참고: [Sqlite / Begin Concurrent](https://www.sqlite.org/cgi/src/doc/begin-concurrent/doc/begin_concurrent.md)

Airflow는 python3로 구동되며 dag역시 python으로 작성한다. 하지만 기존에 python을 사용해보지 않았다고 해도 DAG을 작성하는 것은 문제가 없으며 일반적인 작업에 사용할 operator는 이미 제공이 된다. 설치하면 example DAG이 등록되어 있기 때문에 수정을 해서 사용하면 된다. (Operator를 추가구현 하겠다면 얘기는 달라진다)

### PIP Install

pip 기반 설치는 매우 쉽다. pip만 제대로 설치되어 있으면 한줄이면 끝난다. 

```bash
$ pip install apache-airflow
:
```

### 소스 기반 설치

github 에서 소스코드를 다운로드 받아서 설치한다. 현재는 1.10.9 tag가 가장 최신다.

```bash
# github에서 clone
$ git clone https://github.com/apache/airflow.git
Cloning into 'airflow'...
$ cd airflow
# 1.10.9 태그 checkout
$ git tags -l
$ git checkout tags/1.10.9
# Install
$ python setup.py install
:
Finished processing dependencies for apache-airflow==1.10.9
```

### 설정

#### AIRFLOW_HOME

*_HOME 환경변수는 익숙할 것이다. 공홈 메뉴얼에서는 AIRFLOW_HOME을 ~/airflow 로 가이드하고 있다. 기본설정에서 변경하지 않고 사용할 경우 AIRFLOW_HOME 하위에 다음 파일들이 위치한다. (궂이 변경해서 사용할 필요는 없을 것 같다)

- `airflow.cfg` airflow 설정파일
- `dags` dag 파이썬 파일이 위치하는 디렉토리
- `airflow.db` sqlite를 사용할경우 생성되는 db 파일
- `logs` airflow와 dag의 실행로그파일이 저장되는 디렉토리

#### 설정파일: airflow.cfg

$AIRFLOW_HOME/airflow.cfg 파일이 위치해야 한다. 설치된 파일중 템플릿 설정 파일을 복사한 후 수정하면 된다.

```bash
# MacOS의 경우 python3 site-packages 하위에 airflow 파일들이 설치된다.
ls /usr/local/lib/python3.7/site-packages/apache_airflow-1.10.9-py3.7.egg
# 위 경로에서 설정 템플릿 파일을 복사하자
cp /usr/local/lib/python3.7/site-packages/apache_airflow-1.10.9-py3.7.egg/airflow/config_templates/default_airflow.cfg $AIRFLOW_HOME/airflow.cfg
```

사실상 아무런 설정도 하지 않고 그대로 사용할 수 있다. 그러면 다음 설정으로 동작하게 된다.

- `webserver 주소` 0.0.0.0:8080
- `데이터베이스` sqlite, 파일위치는 $AIRFLOW_HOME/airflow.db
- `dag 파일 경로` $AIRFLOW_HOME/dags

운영환경에서의 기본적인 설정 수정은 다음 정도일 것이다.

```bash
# Airflow 가 DAG을 읽어들일 경로이다. 즉 사용자는 DAG을 작성하여 이 경로에 복사하면 되다.
# 절대경로여야 한다.
dags_folder = {AIRFLOW_HOME}/dags

# 데이터를 저장한 데이터베이스로 아래는 sqlite 의 기본 설정
sql_alchemy_conn = sqlite:////{AIRFLOW_HOME}/airflow.db
# mysql, postgreql 등의 DBMS도 지원하며 관리차원에서는 DBMS를 선택하는 것이 낫다. 아래는 mysql 예시
sql_alchemy_conn = mysql://user:password@mysqlhost:3306/airflow.db

# webserver uri는 아래 세 설정을 사용하면 돈다.
base_url = http://localhost:8080
web_server_host = 0.0.0.0
web_server_port = 8080

# 스케줄러 튜닝 과련 옵션들. Concurrency 옵션은 중요한데 이건 실 운영환경에 따라 차이가 난다.
executor
parallelism
dag_concurrency
max_active_runs_per_dag

# 그외 최근 버전에 LDAP 지원이 추가되었는데 해보지 않았다. 관심있으면 try 해보길..
[ldap]
uri = ..
user_filter = ..
:

```

## 실행

### DB 초기화 및 필요한 프로세스 실행

```bash
# 데이터베이스 초기화. 이 작업은 한번만 해야 한다
$ airflow initdb
# Front-end 역할을 하는 웹서버 실행
$ airflow webserver
# 실제 dag을 실행하는 스케줄러 프로세스를 실행
$ airflow scheduler
```

참고. webserver와 scheduler는 foreground로 실행된다.

## UI 사용

Airflow webser의 UI를 살펴보자. 설명을 위해 두개의 dag을 추가했다.

- [basic_tutorial.py](.resources/airflow/basic_tutorial.py)
- [basic_tutorial_fail.py](.resources/airflow/basic_tutorial_fail.py)

### Dashboard

설정한 URL로 접속하면 airflow에 등록된 dag의 운영상태를 한눈에 알 수 있는 dashboard가 나타난다.

![Airflow dashboard](.resources/airflow/dashboard.png)

특이한 점은 오른쪽위의 시간이 UTC로 표시되는 것을 볼 수 있다. 스크린샷을 찍은 시점은 2020-03-08 16:23 KST이기 때문에 9시간의 차이가 나는데 이게 운영하다보면 은근히 헷갈린다.

- `i` 스케줄 on/off 스위치
- `DAG` 등록된 dag 이름. 클릭하면 dag 상세 페이지로 이동한다.
- `Schedule` Dag에서 지정한 스케줄링 방식을 보여준다. 주의할 것은 이 시간 역시 UTC 기준이다.
- `Owner` 중요하지 않음
- `Recent Tasks` 최근 동작한 task 정보를 상태별로 보여준다.
- `Last Run` 마지막 스케줄 시각. 역시 UTC
- `DAG Runs` Dag 동작 상태
- `Links` DAG의 상세 페이지의 direct link / 스샷이 짤려서 안보이는구만.. 아무튼 있다.

### 여기서 잠깐! schedule 시간에 대해서

Airflow는 스케줄러이기 때문에 "현재시간"이 아니라 "실행시간"에 대해서 이해해야 한다. 일단위 배치작업이 다음처럼 스케줄 될 필요가 있다고 하자.

- 스케줄 시작 시간: 2020-01-01T05:00:00
- 스케줄 정책: 매일 0시 (0 0 * * *)

만약 현재가 2020-01-05T05:00:00 인 경우 이 작업의 스케줄을 실행하면 총 네번 실행되는데 그 "실행시간"은 다음과 같다. 

1. 2020-01-02T00:00:00
2. 2020-01-03T00:00:00
3. 2020-01-04T00:00:00
4. 2020-01-05T00:00:00

Airflow는 dag 코드 내에서 사용할 수 있는 미리 정의된 변수를 제공하는데 그중 실행 시간 관련 변수가 있다. 위의 처리할 일자를 파라미터로 받는 배치 프로그램을 batch operator로 실행한다면 dag은 대충 다음과 같은 모양일 것이다.

```python
args = {
  `start_date`: datetime(2020, 1, 1, 5)
}

dag = DAG{
  schedule_interval=`0 0 * * *`
}

batch_task = BashOperator{
  bash_command=`batch_command {{ ds }}`
  dag=dag
}
```

돌려보진 않았기 때문에 틀렸을 수 있다. 하지만 중요한 것은 start_time, schedule_interval, {{ ds }} 라는 미리 정의된 변수를 통해서 일단위 스케줄을 할 수 있다는 것이다.

참고. [Airflow Default Variables](https://airflow.apache.org/docs/stable/macros.html#default-variables)

### DAG 상세 페이지

basic_tutorial dag의 상세 페이지로 들어가보자.

#### Graph View 

그래프뷰에서는 dag의 의존성 그래프와 현재 실행상태를 보여준다.

![Graph View](.resources/airflow/graph_view.png)

#### 시간 선택

Dag이 스케줄된 시간을 선택하여 정보를 볼 수 있다. 기본은 가장 최근 스케줄 시간이다.

![Time Select](.resources/airflow/time_select.png)

#### 실행 내역의 gantt chart

각 task의 실행 시간을 알 수 있다.

![Gantt Chart](.resources/airflow/gantt_chart.png)

#### Tree view

Tree view 메뉴에서는 DAG내의 각 task에 대해서 각 스케줄마다 성공 실패 여부를 여러 스케줄에 대해서 한번에 확인 가능하다. 각 스케줄 별로 확인하지 않더라도 과거 history 파악이 필요할때 유용하다. 다만 graph view에 비해서 task간 관계 파악은 덜 직관적이다. (나만 그런가?)

![Tree View](.resources/airflow/tree_view.png)

### Task 실행 제어

각 task의 박스를 클릭하면 다음과 같이 task를 제어할 수 있는 메뉴가 팝업된다. 아래는 2020-03-06 01:00 (UTC) 실행된 basic_tutorial_fail dag의 run_after_loop을 클릭했을때 뜨는 팝업이다.

<figure align="middle">
  <img src=".resources/airflow/task_control_popup.png" width="500" title="Gantt Chart"/>
</figure>

기능이 많으니 중요한 몇가지만 살펴보자.

#### Log

Graph view에서 볼 수 있듯이 run_after_loop 태스크는 실패했다. 원인 분석을 위해서 view log를 클릭해보자.

<details><summary>실행로그 </summary>

```java
*** Reading local file: /Users/youngrokko/airflow/logs/basic_tutorial_fail/run_after_loop/2020-03-06T01:00:00+00:00/1.log
[2020-03-08 16:42:15,294] {taskinstance.py:655} INFO - Dependencies all met for <TaskInstance: basic_tutorial_fail.run_after_loop 2020-03-06T01:00:00+00:00 [queued]>
[2020-03-08 16:42:15,304] {taskinstance.py:655} INFO - Dependencies all met for <TaskInstance: basic_tutorial_fail.run_after_loop 2020-03-06T01:00:00+00:00 [queued]>
[2020-03-08 16:42:15,305] {taskinstance.py:866} INFO - 
--------------------------------------------------------------------------------
[2020-03-08 16:42:15,305] {taskinstance.py:867} INFO - Starting attempt 1 of 1
[2020-03-08 16:42:15,305] {taskinstance.py:868} INFO - 
--------------------------------------------------------------------------------
[2020-03-08 16:42:15,312] {taskinstance.py:887} INFO - Executing <Task(BashOperator): run_after_loop> on 2020-03-06T01:00:00+00:00
[2020-03-08 16:42:15,314] {standard_task_runner.py:53} INFO - Started process 13490 to run task
[2020-03-08 16:42:15,368] {logging_mixin.py:112} INFO - Running %s on host %s <TaskInstance: basic_tutorial_fail.run_after_loop 2020-03-06T01:00:00+00:00 [running]> Youngrokui-MacBookPro.local
[2020-03-08 16:42:15,385] {bash_operator.py:82} INFO - Tmp dir root location: 
 /tmp
[2020-03-08 16:42:15,386] {bash_operator.py:105} INFO - Temporary script location: /tmp/airflowtmp5d0zozkw/run_after_loopt40s_5r5
[2020-03-08 16:42:15,387] {bash_operator.py:115} INFO - Running command: echo 1; sleep 5; exit 1
[2020-03-08 16:42:15,392] {bash_operator.py:122} INFO - Output:
[2020-03-08 16:42:15,395] {bash_operator.py:126} INFO - 1
[2020-03-08 16:42:20,403] {bash_operator.py:130} INFO - Command exited with return code 1
[2020-03-08 16:42:20,412] {taskinstance.py:1128} ERROR - Bash command failed
Traceback (most recent call last):
  File "/usr/local/lib/python3.7/site-packages/apache_airflow-1.10.9-py3.7.egg/airflow/models/taskinstance.py", line 966, in _run_raw_task
    result = task_copy.execute(context=context)
  File "/usr/local/lib/python3.7/site-packages/apache_airflow-1.10.9-py3.7.egg/airflow/operators/bash_operator.py", line 134, in execute
    raise AirflowException("Bash command failed")
airflow.exceptions.AirflowException: Bash command failed
[2020-03-08 16:42:20,415] {taskinstance.py:1185} INFO - Marking task as FAILED.dag_id=basic_tutorial_fail, task_id=run_after_loop, execution_date=20200306T010000, start_date=20200308T074215, end_date=20200308T074220
[2020-03-08 16:42:25,289] {logging_mixin.py:112} INFO - [2020-03-08 16:42:25,287] {local_task_job.py:103} INFO - Task exited with return code 1

```

</details>

로그는 airflow 자체 로그와 오퍼레이터에 의해서 실행된 출력결과가 하나의 파일에 저장되어 있다. ERROR - Bash command failed 바로 윗부분을 보면 에러의 원인을 알 수 있다. "Command exited with return code 1"이 바로 그 원인다. run_after_loop는 bash operator인데 다음 명령어를 실행하고 있다. (실행된 명령어는 Rendered Tempalate에서 확인할 수 있다.)

```bash
echo 1; sleep 5; exit 1
```

Bash operator는 명령의 exit code가 0인 경우 성공으로 판단하기 때문에 위 코드는 항상 실패하게 된다.

#### Mark Success

run_after_loop 태스크는 항상 실패하게 되므로 뒤이은 run_this_last는 실행될 수 없다. 이때 강제로 run_after_loop을 성공으로 마킹함으로써 뒤이은 run_this_last가 실행되도록 할 수 있다.

1. run_after_loop 팝업에서 mark success 클릭하여 강제로 성공처리
2. UI 상에서 success가 된 것을 확인
3. run_this_last 태스크 팝업에서 clear를 클릭하여 다시 실행
4. 기다리면 실행된다.

## 운영 이슈

### 타임존

Airflow의 시간은 기본적으로 UTC 기반으로 동작하는데 이게 KST에 사는 우리 입장에선 헷갈리게 만든다. 스케줄러인데 이 dag이 언제 실행될지 헷갈린다는 것은 치명적이다. 다음이 그 케이스.

1. UI에 표기되는 스케줄 관련 시간
2. Dag 파이썬 코드에서 시간 관련 변수

airflow.cfg 에 보면 default_timezone 설정이 있는데 도대체 어디에 써먹는 건지 모르겠다. Asia/seoul로 설정해도 UI 상에 시간이 kst가 되는 것도 아니며 dag 내에서 시간 변수도 여전히 utc이다. 스케줄 시간은 utc만 지원한다는 것이 airflow의 입장이다.

### SPOF

Airflow는 일반적으로 시스템 내에서 매우 중요한 위치를 차지하지만 그 중요도에 비해서 가용성 부분은 취약하다.

- `webserver` Front-end인 webserver는 문제가 생길경우 재실행하면 된다. DAG 코드는 파이썬 파일로저장되며 동작중인 상태는 데이터베이스에 저장되므로 webserver는 stateless라고 할 수 있다.
- `scheduler` Scheduler 프로세스가 다운되는 것은 매우 심각한 상황이다. 실제 dag과 task의 실행을 담당하고 상태와 결과를 DB에 저장하는 역할을 하기 때문에, 프로세스가 다운되면 실행정보가 유실될 뿐만 아니라 더이상 task가 실행되지도 않는다. 복구 또한 까다롭다.

### 자원 점유

Airflow 운영중 가장 큰 문제는 airflow의 자원 점유이다.

`Thread 점유` Airflow 각 task는 thread로 동작한다.
`메모리 부족` Airflow는 동시에 실행되고 있는 task의 수만큼 메모리를 필요로 한다.

Airflow 자원점유의 특징은 DAG이나 task의 총량이 아니라 `동시에 동작 중인 태스크 수`에 선형적으로 비례한다는 점이다. 이로 인해 발생하는 가장 심각한 문제는 **메모리가 부족한 경우 airflow 프로세스가 사라진다**는 점이다.

### UI 반응성

출력할 정보가 많은 경우 속도가 매우 느리다. Task가 많은 경우 graph view를 보는 데는 인내심이 필요하다.

### 초기 기동시간

Airflow scheduler가 죽는 경우 재실행해야하는데 초기 기동시에 dag과 task 정보를 로딩한다. 운영을 위해서는 기동시간에 대한 감이 있어야 한다.
