#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="default"
DEPLOYMENT_FILE="deployment.yaml"
SERVICE_FILE="service.yaml"
DEPLOYMENT_NAME="spring-ceph-client"
CONTAINER_NAME="spring-ceph-client"
SERVICE_NAME="spring-ceph-client"
IMAGE=""
SERVICE_TYPE=""
CREATE_NAMESPACE=false
WAIT_ROLLOUT=true

usage() {
  cat <<'EOF'
Usage: ./deploy-k8s.sh [options]

Options:
  --namespace <name>         Kubernetes namespace (default: default)
  --create-namespace         Create namespace if not exists
  --image <image:tag>        Override deployment container image
  --deployment-file <path>   Deployment yaml path (default: deployment.yaml)
  --service-file <path>      Service yaml path (default: service.yaml)
  --deployment-name <name>   Deployment resource name (default: spring-ceph-client)
  --container-name <name>    Container name in deployment (default: spring-ceph-client)
  --service-name <name>      Service resource name (default: spring-ceph-client)
  --service-type <type>      Service type override: ClusterIP|NodePort|LoadBalancer
  --no-wait                  Skip rollout status wait
  -h, --help                 Show help

Examples:
  ./deploy-k8s.sh --namespace dev --create-namespace
  ./deploy-k8s.sh --namespace dev --image my-registry/spring-ceph-client:v1
  ./deploy-k8s.sh --namespace dev --service-type LoadBalancer
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --namespace)
      NAMESPACE="$2"
      shift 2
      ;;
    --create-namespace)
      CREATE_NAMESPACE=true
      shift
      ;;
    --image)
      IMAGE="$2"
      shift 2
      ;;
    --deployment-file)
      DEPLOYMENT_FILE="$2"
      shift 2
      ;;
    --service-file)
      SERVICE_FILE="$2"
      shift 2
      ;;
    --deployment-name)
      DEPLOYMENT_NAME="$2"
      shift 2
      ;;
    --container-name)
      CONTAINER_NAME="$2"
      shift 2
      ;;
    --service-name)
      SERVICE_NAME="$2"
      shift 2
      ;;
    --service-type)
      SERVICE_TYPE="$2"
      shift 2
      ;;
    --no-wait)
      WAIT_ROLLOUT=false
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      usage
      exit 1
      ;;
  esac
done

if ! command -v kubectl >/dev/null 2>&1; then
  echo "kubectl is not installed or not found in PATH"
  exit 1
fi

if [[ ! -f "$DEPLOYMENT_FILE" ]]; then
  echo "Deployment manifest not found: $DEPLOYMENT_FILE"
  exit 1
fi

if [[ ! -f "$SERVICE_FILE" ]]; then
  echo "Service manifest not found: $SERVICE_FILE"
  exit 1
fi

if [[ -n "$SERVICE_TYPE" ]]; then
  case "$SERVICE_TYPE" in
    ClusterIP|NodePort|LoadBalancer)
      ;;
    *)
      echo "Invalid --service-type: $SERVICE_TYPE"
      echo "Allowed values: ClusterIP | NodePort | LoadBalancer"
      exit 1
      ;;
  esac
fi

if [[ "$CREATE_NAMESPACE" == true ]]; then
  if ! kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
    echo "Creating namespace: $NAMESPACE"
    kubectl create namespace "$NAMESPACE"
  fi
fi

echo "Applying deployment manifest: $DEPLOYMENT_FILE"
kubectl apply -n "$NAMESPACE" -f "$DEPLOYMENT_FILE"

echo "Applying service manifest: $SERVICE_FILE"
kubectl apply -n "$NAMESPACE" -f "$SERVICE_FILE"

if [[ -n "$SERVICE_TYPE" ]]; then
  echo "Patching service type: service/$SERVICE_NAME -> $SERVICE_TYPE"
  kubectl patch service "$SERVICE_NAME" \
    -n "$NAMESPACE" \
    --type merge \
    -p "{\"spec\":{\"type\":\"$SERVICE_TYPE\"}}"
fi

if [[ -n "$IMAGE" ]]; then
  echo "Updating image: deployment/$DEPLOYMENT_NAME $CONTAINER_NAME=$IMAGE"
  kubectl set image deployment/"$DEPLOYMENT_NAME" \
    "$CONTAINER_NAME=$IMAGE" \
    -n "$NAMESPACE"
fi

if [[ "$WAIT_ROLLOUT" == true ]]; then
  echo "Waiting for rollout: deployment/$DEPLOYMENT_NAME"
  kubectl rollout status deployment/"$DEPLOYMENT_NAME" -n "$NAMESPACE"
fi

echo "Service summary:"
kubectl get service "$SERVICE_NAME" -n "$NAMESPACE" -o wide

CURRENT_SERVICE_TYPE=$(kubectl get service "$SERVICE_NAME" -n "$NAMESPACE" -o jsonpath='{.spec.type}')
if [[ "$CURRENT_SERVICE_TYPE" == "LoadBalancer" ]]; then
  EXTERNAL_IP=$(kubectl get service "$SERVICE_NAME" -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
  if [[ -z "$EXTERNAL_IP" ]]; then
    EXTERNAL_IP=$(kubectl get service "$SERVICE_NAME" -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
  fi

  if [[ -n "$EXTERNAL_IP" ]]; then
    echo "External endpoint ready: $EXTERNAL_IP"
  else
    echo "External endpoint is pending."
    echo "If this is bare-metal, install/configure MetalLB or use NodePort/Ingress with external L4/L7."
  fi
fi

echo "Kubernetes deployment completed"
