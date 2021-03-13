# Google Cloud Storage Integrations

이 문서에서는 어쩌고 저쩌고, 이하 GCS 로 칭함

## Google Cloud Storage 개요

[GCS 설명](https://cloud.google.com/storage?hl=ko)

## 구글 클라우드 설정

gcloud 설정

|항목|설정 값|
|:-:|-|
| default zone | asia-northeast3-ab |
| project | youngrok |
| bucket | youngrok |

## 연동 및 사용

테스트 환경

- Intel macOS + docker desktop
- HDP 3.0.1 Docker 버전

gsutil 설정 및 설치
테스트를 위해서 google cloud sdk 설치와 인증이 필요하다. 참고: https://cloud.google.com/storage/docs/gsutil_install?hl=ko#linux

참고로 하둡 관련 예제는 모두 hdfs 계정을 통해서 수행한다. 앞으로 예제에서는 도커 접속과 hdfs로의 유저 변경 명령어는 생략한다.

```bash
# host 에서 container 접속 (bash 실행)
docker exec -it sandbox-hdp /bin/bash
# hdfs 로 사용자 변경
sudo su - hdfs
# download x86 64bit sdk 다운로드
curl -O https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-330.0.0-linux-x86_64.tar.gz
tar -xzf google-cloud-sdk-330.0.0-linux-x86_64.tar.gz
# 설치 및 설정
./google-cloud-sdk/install.sh
./google-cloud-sdk/bin/gcloud init
```

- 항목 1
- 항목 2

### HDFS 인터페이스

HDFS 파일시스템은 인터페이스에 hdfs 구현체가 붙어 있는 식임. hdfs 구현체 대신 s3, gcs 구현체를 붙임으로써 다른 스토리지로 사용할 수도 있다.

HDFS 관련 명령어는 다음과 같이 사용한다.

```bash
hdfs dfs -ls /user/hdfs
```

이중 마지막 path 부분을 local 경로처럼 사용하지만 사실 이 명령어의 완성된 형태는 다음과같다.

```bash
hdfs dsf -ls hdfs://....//user/hdfs
```

완성된 형태에서는 두가지 정보가 추가되어 있다.

1. `hdfs://` 대상 파일시스템이 HDFS이다. (hdfs 프로토콜을 사용한다.)
2. `....` 호스트가 ... 이다.

hdfs 와 호스트네임을 생략하는 것은 core-site.xml 의 기본 파일시스템이 설정되어 있기 때문다.

```xml
<property>
  <name></name>
  <></>
  <final>true</true>
</property>
```

이중 1번을 보면 이런 생각이 들 것이다. `HDFS가 아니어도 되겠는데?` 맞다. 실제 구현을 보면 인터페이스와 파일시스템 구현체가 분리되어 있고 hdfs 는 그 구현체중 하나이다. (VFS를 생각하면 될 듯하다.) 이 인터페이스와 호환되는 프로토콜 구현체를 제공하는 파일시스템, 예를 들어 S3, GCS, Ceph 등은 hdfs 에서 직접적으로 사용할 수 있다.

테스트를 위해서 HDP 3.1 사용

#### distcp 예제

hadoop fs distcp gs://..

이때 distcp 의 체크섬 기능은 사용할 수 없다. (요건 확인 필요)

#### hive 파티션에 gs 입력

Hive는 external table을 사용하여 데이터가 저장될 파티션의 경로를 지정할 수 있다. 이 경로를 GCS로 지정가능하다. 

```HQL
CREATE EXTERNAL TABLE IF NOT EXIST
..
..
..
파티션 위치: ..
```

- 몇줄 인서트

- GCS 에 생긴 파티션 파일 보임



#### default fs 변경에 대한 여담

Default FS를 변경하는 것은 궂이 생각할 필요도 없는 거지만 테스트는 해봤으니까 몇자 적어본다.

"Default fs가 변경가능하다면 s3, gcs 상에서 하둡을 사용할 수 있겠네?" 라는 의문이 들 수 있다. 답은 `가능하다`이다. 하지만 내 의견은 `그런 짓은 하지마라`이다. 이 예제에서도 그렇고 하둡을 사용하는 일반적인 패턴는 hdp와 같은 배포판을 설치하는 것이고 배포판은 ambari와 같은 운영 도구를 가지고 있다. 이들 운영도구는 defaultFS에 기본적으로 설치하는 일종의 시스템파일에 해당하는 파일이 있다. 기본 파일시스템을 변경할 경우 이들 파일을 모두 이관하고 권한을 설정하는 등의 일을 해야 한다. 만약 이 파일 속에 defaultFS 의 url 이 절대경로로 박혀 있다면 전부 찾아서 변경해야 한다.

당장 HDP의 구성 요소를 보면 저장소인 HDFS를 제외하면 모두(?) 프로세싱 기능을 포함하고 있다. Map reduce든 spark이든 hive든 결국은 프로세스로 동작해야하는데 S3나 GCS자체로는 이런 기능을 제공하지 않는다. 따라서 HDP를 운영하면서 defaultFS를 프로세싱 기능이 없는 S3, GCS로 지정한다는 것은 저장소 전용으로 운영한다는 거나 다름 없는데, 궂이 그런 일을 하는 것은 완전히 쓸데 없는 짓이다. 당장 distcp 만 생각해보라. Distcp 는 MapReduce 를 사용하는데, 프로세싱에 사용할 장비가 없는 경우 당연히 동작하지 않는다. 즉 다른 하둡 클러스터나 EMR, DataProc 에서 수행해야 한다는 것이다. 그럴거면 뭐하러 HDP 클러스터를 설치하겠나. `Ambari 실습해보려고?:-D` 만약 내 HDP클러스터에서 프로세싱을 돌리되 데이터 저장은 S3, GCS에 하고 싶다면 장비 스펙을 core, ram에 맞추고 MapReduce의 중간파일이나 spark history가 저장될 정도의 작은 hdfs구성하고, S3나 GCS는 별도의 외부 클러스터처럼 사융하라.

### 빅쿼리

빅쿼리에는 avro, parquet, orc, csv, tsv, json 파일을 사용해 일괄 얼로드 가능하다. 이 혜에서는 gcs에 있는 tsv 파일을 빅쿼리 테이블에 적재하는 예를 보인다. 예시 스텝은 다음과 같다.

1. gcs에 csv 업로드 (gsutil 사용)
2. gcs에서 빅쿼리 테이블로 csv 업로드 (bq 사용)

업로드할 tsv 파일은 다음과 같다. ([다운로드링크](.resources/gcp_storage_integrations/bigquery_data.csv)) 첫행은 빅쿼리 테이블의 컬럼 이름으로 사용된다.

```csv
Name,Position,Age,From
Cesc Fabregas,Midfielder,35,Spain
Robin Van Persi,Forward,38,Holand
Samir Nasri,Midfielder,33,France
Alexis Sanchez,Forward,30,Chile
```

위 csv 파일을 업로드한다. 빅쿼리는 gzip 압축된 파일도 import 가능하지만 비압축된 파일보다 속도가 느리다. 물론 업로드는 더 빠르겠지.

```bash
# 업로드
$ gsutil cp bigquery_data.csv gs://youngrok_storage/
Copying file://bigquery_data.tsv [Content-Type=text/tab-separated-values]...
/ [1 files][  175.0 B/  175.0 B]
Operation completed over 1 objects/175.0 B.

# 업로드 확인
$ gsutil ls -l gs://youngrok_storage/
       162  2021-03-07T14:35:42Z  gs://youngrok_storage/bigquery_data.csv
TOTAL: 1 objects, 162 bytes (162 B)

# 빅쿼리 import
$ bq load --autodetect --source_format=CSV --skip_leading_rows=1 gs://youngrok_storage bigquery_data.tsv gcs_upload.arsenal_players
Waiting on bqjob_r6a4e168b4d38df5d_000001780d2088aa_1 ... (0s) Current status: DONE
```

콘솔에서 확인한 생성된 테이블

![생성된 테이블](.resources/gcp_storage_integrations/uploaded_bigquery_table.png)

참고문서

1. [gsutil을 사용한 업로드](https://cloud.google.com/storage/docs/uploading-objects?hl=ko#gsutil)
2. [Cloud Storage에서 CSV 데이터 로드](https://cloud.google.com/bigquery/docs/loading-data-cloud-storage-csv?hl=ko#bq)

빅쿼리 import에는 훨씬 다양한 옵션이 존재하는데 이 예는 빅쿼리가 아닌 gcs에 관한 예제이므로 상세 옵션은 설명하지 않는다.

### Http direct access

기본적으로 웹에서 읽을 수 있음
스크린 샷

### Http through CloudCDN

CloudCDN 은 여러가지 백엔드를 가짐. 그중에 GCS가 가능함

### Log bucket

[로그 버킷](https://cloud.google.com/logging/docs/buckets)

### Migration (Data Transfer)

S3, On premise

### API (java 예제)

### Spring



### 기타 코드기반 product

다음 product 은 연동이 된다.

- DataFlow
- DataProc

등등 여러 product 에서 가능하지만 이건 그냥 템플릿 코드를 사용하며 그속에 API가 있는 것이므로 딱히 언급할 필요는 없을 것 같다.
