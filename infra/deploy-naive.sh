#!/usr/bin/env bash
# 단일 배포(before) 재현용: ALB에서 빼지 않고 모든 인스턴스의 컨테이너를 "동시에" 교체한다.
# = 각 서버에서 docker rm -f && docker run 을 한꺼번에 하는 것과 같다 → 그동안 요청이 실패한다.
set -euo pipefail
export AWS_REGION=ap-southeast-2

IDS=$(aws autoscaling describe-auto-scaling-groups --auto-scaling-group-names umc-asg \
        --query 'AutoScalingGroups[0].Instances[?LifecycleState==`InService`].InstanceId' \
        --output text)
echo "동시 교체 대상: $IDS"

CID=$(aws ssm send-command --instance-ids $IDS --document-name AWS-RunShellScript \
        --parameters 'commands=["bash /opt/umc/swap.sh"]' \
        --query Command.CommandId --output text)

# 모든 인스턴스에서 교체 명령이 끝날 때까지 기다린다
while aws ssm list-command-invocations --command-id "$CID" \
        --query 'CommandInvocations[].Status' --output text | grep -qE 'Pending|InProgress'; do
  sleep 3
done
aws ssm list-command-invocations --command-id "$CID" \
  --query 'CommandInvocations[].{id:InstanceId,status:Status}' --output table
echo "교체 명령 완료. 서버가 다시 healthy 가 될 때까지 측정을 계속 두세요."
