# Google Cloud CloudRun ColdStart 대응

## 문제

기본적으로 CloudRun은 인스턴스가 생성되면 트래픽을 라우팅한다. 즉 인스턴스가 실행하는 앱의 서비스 rediness 체크는 없으며, cold start 가 긴 앱의 경우 서비스가 가용해지기 전에 호출될 수 있다. 이는 첫번째 배포에서는 문제가 되지 않는다. 하지만 이후 새버전을 재배포할때 앱이 서비스 준비가 되기 전에 새버전 인스턴스로 트래픽이 라우팅된다. 반복 호출 테스트한 결과 호출이 실패하지는 않지만 신규 인스턴스의 초반 리턴 시간이 길어진다.

## 문제 대응

다음은 CloudRun 재배포(update) 명령어 레퍼런스이다. (레퍼런스 치고는 보기 편하고 커맨드 편집 기능도 제공한다.)

[Cloud SDK reference: Cloud Run 재배포](https://cloud.google.com/sdk/gcloud/reference/run/services/update)

### CloudRun 의 지속배포 설정

지속 배포 설정으로 cloud run 서비스를 생성하면 cloud build api에 트리거가 자동으로 생성된다. 트리거는 다음 기능을 한다.

1. dockerize
2. Container registry 에 이미지 push
3. Cloudrun 서비스에 (재)배포

아래는 트리거의 inline 스크립트이다. (자동 생성된다)

```yaml
steps:
  - name: gcr.io/cloud-builders/docker
    args:
      - build
      - '--no-cache'
      - '-t'
      - '$_GCR_HOSTNAME/$PROJECT_ID/$REPO_NAME/$_SERVICE_NAME:$COMMIT_SHA'
      - .
      - '-f'
      - Dockerfile
    id: Build
  - name: gcr.io/cloud-builders/docker
    args:
      - push
      - '$_GCR_HOSTNAME/$PROJECT_ID/$REPO_NAME/$_SERVICE_NAME:$COMMIT_SHA'
    id: Push
  - name: gcr.io/google.com/cloudsdktool/cloud-sdk
    args:
      - run
      - services
      - update
      - $_SERVICE_NAME
      - '--platform=managed'
      - '--image=$_GCR_HOSTNAME/$PROJECT_ID/$REPO_NAME/$_SERVICE_NAME:$COMMIT_SHA'
      - >-
        --labels=managed-by=gcp-cloud-build-deploy-cloud-run,commit-sha=$COMMIT_SHA,gcb-build-id=$BUILD_ID,gcb-trigger-id=$_TRIGGER_ID,$_LABELS
      - '--region=$_DEPLOY_REGION'
      - '--quiet'
    id: Deploy
    entrypoint: gcloud
images:
  - '$_GCR_HOSTNAME/$PROJECT_ID/$REPO_NAME/$_SERVICE_NAME:$COMMIT_SHA'
options:
  substitutionOption: ALLOW_LOOSE
substitutions:
  _GCR_HOSTNAME: asia.gcr.io
  _PLATFORM: managed
  _SERVICE_NAME: asia-northeast3
  _DEPLOY_REGION: ...
  _LABELS: gcb-trigger-id=094ec2ca-f389-459f-aaf4-cfc3c94a4eb9
  _TRIGGER_ID: 094ec2ca-f389-459f-aaf4-cfc3c94a4eb9
tags:
  - gcp-cloud-build-deploy-cloud-run
  - gcp-cloud-build-deploy-cloud-run-managed
  - ...
```

스크립트에서 중요한 부분은 마지막 스텝이다. 다음은 이 부분만 명령어로 적은 것이다. (실제 동작하는 명령어이다.)

```bash
gcloud run run services update $_SERVICE_NAME --platform=managed --image=$_GCR_HOSTNAME/$PROJECT_ID/$REPO_NAME/$_SERVICE_NAME:$COMMIT_SHA --labels=managed-by=gcp-cloud-build-deploy-cloud-run,commit-sha=$COMMIT_SHA,gcb-build-id=$BUILD_ID,gcb-trigger-id=$_TRIGGER_ID,$_LABELS --region=$_DEPLOY_REGION --quiet
```

이 명령어가 실행되면 "문제" 단락에서 설명한 것처럼 배포와 트래픽 전환이 한번에 일어나게 된다.

### 배포와 트래픽 전환 분리

배포와 트래픽 전환을 분리할 필요가 있다. 이는 --no-traffic 옵션과 update-traffic 명령어로 가능하다.

--no-traffic 옵션을 사용하면 트래픽 전환 없이 배포할 수 있다.

```bash
gcloud run run services update $_SERVICE_NAME --platform=managed --image=$_GCR_HOSTNAME/$PROJECT_ID/$REPO_NAME/$_SERVICE_NAME:$COMMIT_SHA --labels=managed-by=gcp-cloud-build-deploy-cloud-run,commit-sha=$COMMIT_SHA,gcb-build-id=$BUILD_ID,gcb-trigger-id=$_TRIGGER_ID,$_LABELS --region=$_DEPLOY_REGION --quiet --no-traffic
```

그리고 다음 명령어를 통해서 트래픽을 "마지막에 배포한 버전"으로 전환할 수 있다.

```bash
gcloud run services update-traffic --region=$_DEPLOY_REGION $_SERVICE_NAME --platform=managed --to-latest
```

### Cold Start 를 감안한 스크립트

Cold start를 기다린 후 트래픽을 전환하려면 다음과 같이 해야 하다.

1. --no-traffic 옵션을 주고 배포
2. Cold start 완료 대기
3. 트래픽 전환

문제는 2번을 수행할 방법인데 현재(2020-12-07)까진 찾지 못했다. 그래서 그냥 sleep 을 줬다. 아래는 sleep을 추가한 빌드 스크립트이다.

```yaml
steps:
  - name: gcr.io/cloud-builders/docker
    args:
      - build
      - '--no-cache'
      - '-t'
      - '$_GCR_HOSTNAME/$PROJECT_ID/$REPO_NAME/$_SERVICE_NAME:$COMMIT_SHA'
      - .
      - '-f'
      - Dockerfile
    id: Build
  - name: gcr.io/cloud-builders/docker
    args:
      - push
      - '$_GCR_HOSTNAME/$PROJECT_ID/$REPO_NAME/$_SERVICE_NAME:$COMMIT_SHA'
    id: Push
  - name: gcr.io/google.com/cloudsdktool/cloud-sdk
    args:
      - beta
      - run
      - services
      - update
      - $_SERVICE_NAME
      - '--platform=managed'
      - '--no-traffic'
      - '--image=$_GCR_HOSTNAME/$PROJECT_ID/$REPO_NAME/$_SERVICE_NAME:$COMMIT_SHA'
      - >-
        --labels=managed-by=gcp-cloud-build-deploy-cloud-run,commit-sha=$COMMIT_SHA,gcb-build-id=$BUILD_ID,gcb-trigger-id=$_TRIGGER_ID,$_LABELS
      - '--region=$_DEPLOY_REGION'
      - '--quiet'
      - '--min-instances=1'
    id: Deploy
    entrypoint: gcloud
  - name: ubuntu
    args:
      - sleep
      - '10'
    id: WaitColdStart
  - name: gcr.io/google.com/cloudsdktool/cloud-sdk
    args:
      - run
      - services
      - update-traffic
      - '--region=$_DEPLOY_REGION'
      - $_SERVICE_NAME
      - '--platform=managed'
      - '--to-latest'
    id: Traffic
    entrypoint: gcloud
images:
  - '$_GCR_HOSTNAME/$PROJECT_ID/$REPO_NAME/$_SERVICE_NAME:$COMMIT_SHA'
options:
  substitutionOption: ALLOW_LOOSE
substitutions:
  _LABELS: gcb-trigger-id=0bc33299-b74d-4368-8233-366a2943ebe1
  _TRIGGER_ID: 0bc33299-b74d-4368-8233-366a2943ebe1
  _DEPLOY_REGION: asia-northeast3
  _GCR_HOSTNAME: asia.gcr.io
  _PLATFORM: managed
  _SERVICE_NAME: ...
tags:
  - gcp-cloud-build-deploy-cloud-run
  - gcp-cloud-build-deploy-cloud-run-managed
  - ...
```

WaitColdStart 스텝이 sleep 10초 부분, 즉 cold start가 끝나길 기다리는 부분이다. 10초라는 magic number 가 불편하다. 하지만 cold start 시간까지 예측할 수는 없기 때문에 적당히 변수처리해서 앱마다(정확히는 트리거마다) 대기 시간을 다르게 설정하여 '덜' 불편하게 쓸 수는 있다.

## tags

GCP, CloudRun, CloudRun ColdStart
