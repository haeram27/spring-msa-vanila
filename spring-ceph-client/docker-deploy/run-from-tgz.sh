#!/usr/bin/env bash
set -euo pipefail

ARCHIVE_FILE="${ARCHIVE_FILE:-spring-ceph-client_latest.tgz}"
IMAGE_NAME="${IMAGE_NAME:-spring-ceph-client}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
CONTAINER_NAME="${CONTAINER_NAME:-spring-ceph-client}"
HOST_PORT="${HOST_PORT:-31349}"
CONTAINER_PORT="${CONTAINER_PORT:-31349}"
NETWORK_NAME="${NETWORK_NAME:-}"

CEPH_AWS_S3_ENDPOINT="${CEPH_AWS_S3_ENDPOINT:-}"
CEPH_AWS_S3_REGION="${CEPH_AWS_S3_REGION:-}"
CEPH_AWS_S3_ACCESS_KEY="${CEPH_AWS_S3_ACCESS_KEY:-}"
CEPH_AWS_S3_SECRET_KEY="${CEPH_AWS_S3_SECRET_KEY:-}"
CEPH_AWS_S3_PATH_STYLE_ACCESS_ENABLED="${CEPH_AWS_S3_PATH_STYLE_ACCESS_ENABLED:-true}"

usage() {
  cat <<'EOF'
Usage: ./docker/run-from-tgz.sh [options]

Options:
  --archive <path>                         Compressed image archive path (.tgz or .tar.gz) [required]
  --image <name>                           Target Docker image name (default: spring-ceph-client)
  --tag <tag>                              Target Docker image tag (default: latest)
  --container <name>                       Container name (default: spring-ceph-client)
  --host-port <port>                       Host port to publish (default: 31349)
  --container-port <port>                  Container port to expose (default: 31349)
  --network <name>                         Optional Docker network name

  --ceph-aws-s3-endpoint <value>           Set CEPH_AWS_S3_ENDPOINT
  --ceph-aws-s3-region <value>             Set CEPH_AWS_S3_REGION
  --ceph-aws-s3-access-key <value>         Set CEPH_AWS_S3_ACCESS_KEY
  --ceph-aws-s3-secret-key <value>         Set CEPH_AWS_S3_SECRET_KEY
  --ceph-aws-s3-path-style-access-enabled  Set CEPH_AWS_S3_PATH_STYLE_ACCESS_ENABLED (default: true)

  -h, --help                               Show this help

Examples:
  ./docker/run-from-tgz.sh \
    --archive ./spring-ceph-client_latest.tgz \
    --image spring-ceph-client \
    --tag latest \
    --container spring-ceph-client \
    --ceph-aws-s3-endpoint http://127.0.0.1:8555 \
    --ceph-aws-s3-region us-east-1 \
    --ceph-aws-s3-access-key <access-key> \
    --ceph-aws-s3-secret-key <secret-key> \
    --ceph-aws-s3-path-style-access-enabled true
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --archive)
      ARCHIVE_FILE="$2"
      shift 2
      ;;
    --image)
      IMAGE_NAME="$2"
      shift 2
      ;;
    --tag)
      IMAGE_TAG="$2"
      shift 2
      ;;
    --container)
      CONTAINER_NAME="$2"
      shift 2
      ;;
    --host-port)
      HOST_PORT="$2"
      shift 2
      ;;
    --container-port)
      CONTAINER_PORT="$2"
      shift 2
      ;;
    --network)
      NETWORK_NAME="$2"
      shift 2
      ;;
    --ceph-aws-s3-endpoint)
      CEPH_AWS_S3_ENDPOINT="$2"
      shift 2
      ;;
    --ceph-aws-s3-region)
      CEPH_AWS_S3_REGION="$2"
      shift 2
      ;;
    --ceph-aws-s3-access-key)
      CEPH_AWS_S3_ACCESS_KEY="$2"
      shift 2
      ;;
    --ceph-aws-s3-secret-key)
      CEPH_AWS_S3_SECRET_KEY="$2"
      shift 2
      ;;
    --ceph-aws-s3-path-style-access-enabled)
      CEPH_AWS_S3_PATH_STYLE_ACCESS_ENABLED="$2"
      shift 2
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

if [[ -z "$ARCHIVE_FILE" ]]; then
  echo "--archive is required"
  usage
  exit 1
fi

if [[ ! -f "$ARCHIVE_FILE" ]]; then
  echo "Archive file not found: $ARCHIVE_FILE"
  exit 1
fi

FULL_IMAGE="${IMAGE_NAME}:${IMAGE_TAG}"

if docker ps --format '{{.Names}}' | grep -Fxq "$CONTAINER_NAME"; then
  echo "[1/5] Stopping running container: $CONTAINER_NAME"
  docker stop "$CONTAINER_NAME" >/dev/null
else
  echo "[1/5] Running container not found: $CONTAINER_NAME"
fi

if docker ps -a --format '{{.Names}}' | grep -Fxq "$CONTAINER_NAME"; then
  echo "[2/5] Removing existing container: $CONTAINER_NAME"
  docker rm "$CONTAINER_NAME" >/dev/null
else
  echo "[2/5] Existing container not found: $CONTAINER_NAME"
fi

if docker image inspect "$FULL_IMAGE" >/dev/null 2>&1; then
  echo "[3/5] Removing existing image: $FULL_IMAGE"
  docker rmi "$FULL_IMAGE"
else
  echo "[3/5] Existing image not found: $FULL_IMAGE"
fi

echo "[4/5] Loading image archive: $ARCHIVE_FILE"
LOAD_OUTPUT="$(docker load -i "$ARCHIVE_FILE" 2>&1)"
echo "$LOAD_OUTPUT"

if ! docker image inspect "$FULL_IMAGE" >/dev/null 2>&1; then
  LOADED_IMAGE_REF="$(echo "$LOAD_OUTPUT" | sed -n 's/^Loaded image: //p' | tail -n 1)"
  if [[ -n "$LOADED_IMAGE_REF" ]]; then
    echo "Tagging loaded image as target: $FULL_IMAGE"
    docker tag "$LOADED_IMAGE_REF" "$FULL_IMAGE"
  fi
fi

if ! docker image inspect "$FULL_IMAGE" >/dev/null 2>&1; then
  echo "Failed to find target image after load: $FULL_IMAGE"
  exit 1
fi

RUN_CMD=(
  docker run -d
  --name "$CONTAINER_NAME"
  -p "${HOST_PORT}:${CONTAINER_PORT}"
)

if [[ -n "$NETWORK_NAME" ]]; then
  RUN_CMD+=(--network "$NETWORK_NAME")
fi

if [[ -n "$CEPH_AWS_S3_ENDPOINT" ]]; then
  RUN_CMD+=(-e "CEPH_AWS_S3_ENDPOINT=$CEPH_AWS_S3_ENDPOINT")
fi
if [[ -n "$CEPH_AWS_S3_REGION" ]]; then
  RUN_CMD+=(-e "CEPH_AWS_S3_REGION=$CEPH_AWS_S3_REGION")
fi
if [[ -n "$CEPH_AWS_S3_ACCESS_KEY" ]]; then
  RUN_CMD+=(-e "CEPH_AWS_S3_ACCESS_KEY=$CEPH_AWS_S3_ACCESS_KEY")
fi
if [[ -n "$CEPH_AWS_S3_SECRET_KEY" ]]; then
  RUN_CMD+=(-e "CEPH_AWS_S3_SECRET_KEY=$CEPH_AWS_S3_SECRET_KEY")
fi
RUN_CMD+=(-e "CEPH_AWS_S3_PATH_STYLE_ACCESS_ENABLED=$CEPH_AWS_S3_PATH_STYLE_ACCESS_ENABLED")

RUN_CMD+=("$FULL_IMAGE")

echo "[5/5] Running container with image: $FULL_IMAGE"
"${RUN_CMD[@]}"

echo "Done"
echo "Container: $CONTAINER_NAME"
echo "Image: $FULL_IMAGE"
