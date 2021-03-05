# Google Cloud Storage Integrations

이 문서에서는 어쩌고 저쩌고, 이하 GCS 로 칭함

## Google Cloud Storage 개요

[GCS 설명](https://cloud.google.com/storage?hl=ko)

## 연동

준비작업

Bucket 생성

### Cloud SDK

gsutil

### HDFS 인터페이스

HDFS 파일시스템은 인터페이스에 hdfs 구현체가 붙어 있는 식임. hdfs 구현체 대신 s3, gcs 구현체를 붙임으로써 다른 스토리지로 사용할 수도 있다.

테스트를 위해서 HDP 3.1 사용

hadoop fs distcp gs://..

이때 distcp 의 체크섬 기능은 사용할 수 없음

### 빅쿼리

csv, tsv 파일을 빅쿼리로 업로드 가능하다.
GCS 에 tsv 파일을 압축해서 업로드. import.
skip-header 기능

### Http direct access

기본적으로 웹에서 읽을 수 있음

### Http through CloudCDN

CloudCDN 은 여러가지 백엔드를 가짐. 그중에 GCS가 가능함

### Log bucket

[로그 버킷](https://cloud.google.com/logging/docs/buckets)

### Migration

S3, On premise

### 기타 구글 코드들

Dataflow

DataProc

AI Platform
