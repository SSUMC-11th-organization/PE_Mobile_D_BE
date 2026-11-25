#!/usr/bin/env bash
set -euo pipefail
export AWS_REGION=ap-southeast-2   # 추가: 프로젝트 리전(시드니)

# 3.8 태깅 정책: 버전-yy-mm-dd-N (예: v1.0.0-26-11-22-1)
VERSION=${1:?"사용법: bash infra/deploy.sh v1.0.0"}
PREFIX="${VERSION}-$(date +%y-%m-%d)-"
N=$(aws ecr list-images --repository-name umc-app --filter tagStatus=TAGGED \
      --query "length(imageIds[?starts_with(imageTag, '${PREFIX}')])" --output text)
TAG="${PREFIX}$((N + 1))"
echo "이번 배포 태그: ${TAG}"

ECR=$(aws ecr describe-repositories --repository-names umc-app \
        --query 'repositories[0].repositoryUri' --output text)
TG=$(aws elbv2 describe-target-groups --names umc-app-tg \
       --query 'TargetGroups[0].TargetGroupArn' --output text)

# 1. 이미지
aws ecr get-login-password --region ap-southeast-2 \
  | docker login --username AWS --password-stdin "$ECR"
docker buildx build --platform linux/arm64 --provenance=false -t "${ECR}:${TAG}" --push .

# 2. 앞으로 "새로 뜨는" 인스턴스가 읽을 태그
aws ssm put-parameter --name /umc/app/image --type String --value "${ECR}:${TAG}" --overwrite

# 3. "지금 떠 있는" 인스턴스를 한 대씩 교체
# InService 만 고릅니다 — Terminating 인 인스턴스를 교체하려 들면 거기서 멈춥니다
for ID in $(aws autoscaling describe-auto-scaling-groups --auto-scaling-group-names umc-asg \
              --query 'AutoScalingGroups[0].Instances[?LifecycleState==`InService`].InstanceId' \
              --output text); do
  echo "── $ID"

  # 트래픽을 끊는다. 처리 중이던 요청은 deregistration_delay 만큼 기다려 준다
  aws elbv2 deregister-targets --target-group-arn "$TG" --targets Id="$ID"
  aws elbv2 wait target-deregistered --target-group-arn "$TG" --targets Id="$ID"

  # 컨테이너 교체
  CID=$(aws ssm send-command --instance-ids "$ID" --document-name AWS-RunShellScript \
          --parameters 'commands=["bash /opt/umc/swap.sh"]' \
          --query Command.CommandId --output text)

  # aws ssm wait 는 5초 × 20회 = 100초가 상한이다. 이미지를 처음 받으면 모자랄 수 있어
  # 직접 돈다. 실패하면 로그를 보여주고 멈춘다 — 다음 대로 넘어가면 안 된다.
  until [ "$(aws ssm list-command-invocations --command-id "$CID" \
               --query 'CommandInvocations[0].Status' --output text)" != "InProgress" ]; do
    sleep 5
  done
  ST=$(aws ssm list-command-invocations --command-id "$CID" \
         --query 'CommandInvocations[0].Status' --output text)
  if [ "$ST" != "Success" ]; then
    echo "교체 실패($ST). 이 인스턴스는 타겟에서 빠진 채로 남아 있다."
    aws ssm list-command-invocations --command-id "$CID" --details \
      --query 'CommandInvocations[0].CommandPlugins[0].Output' --output text | tail -20
    exit 1
  fi

  # 다시 트래픽을 받는다. 정상이 될 때까지 기다린 뒤에야 다음 대로 넘어간다
  aws elbv2 register-targets --target-group-arn "$TG" --targets Id="$ID"
  aws elbv2 wait target-in-service --target-group-arn "$TG" --targets Id="$ID"
done
