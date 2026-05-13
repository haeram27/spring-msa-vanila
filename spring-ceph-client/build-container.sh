#!/usr/bin/env bash
set -euo pipefail

IMAGE_NAME="spring-ceph-client"
IMAGE_TAG="latest"
JAR_FILE=""
DOCKERFILE="Dockerfile"
BUILD_JAR=true
PUSH_IMAGE=false
SAVE_IMAGE_ARCHIVE=false
ARCHIVE_FILE=""

usage() {
  cat <<'EOF'
Usage: ./build-container.sh [options]

Options:
  --image <name>       Docker image name (default: spring-ceph-client)
  --tag <tag>          Docker image tag (default: latest)
  --jar <path>         Jar path to package (default: auto-detect build/libs/*.jar)
  --dockerfile <path>  Dockerfile path (default: Dockerfile)
  --no-build           Skip jar build step
  --push               Push image after build
  --save-archive       Save image as tar.gz for later docker load use (default: disabled)
  --archive-file <p>   Output archive path (default: <image>_<tag>.tar.gz)
  -h, --help           Show this help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --image)
      IMAGE_NAME="$2"
      shift 2
      ;;
    --tag)
      IMAGE_TAG="$2"
      shift 2
      ;;
    --jar)
      JAR_FILE="$2"
      shift 2
      ;;
    --dockerfile)
      DOCKERFILE="$2"
      shift 2
      ;;
    --no-build)
      BUILD_JAR=false
      shift
      ;;
    --push)
      PUSH_IMAGE=true
      shift
      ;;
    --save-archive)
      SAVE_IMAGE_ARCHIVE=true
      shift
      ;;
    --archive-file)
      ARCHIVE_FILE="$2"
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

if [[ "$BUILD_JAR" == true ]]; then
  echo "[1/3] Building jar (skip tests): gradle clean bootJar -x test"
  gradle clean bootJar -x test
fi

if [[ -z "$JAR_FILE" ]]; then
  mapfile -t jars < <(ls -1t build/libs/*.jar 2>/dev/null || true)
  if [[ ${#jars[@]} -eq 0 ]]; then
    echo "No jar found under build/libs. Run gradle bootJar first or pass --jar."
    exit 1
  fi
  JAR_FILE="${jars[0]}"
fi

if [[ ! -f "$JAR_FILE" ]]; then
  echo "Jar file not found: $JAR_FILE"
  exit 1
fi

echo "[2/3] Building container image"
FULL_IMAGE="${IMAGE_NAME}:${IMAGE_TAG}"
docker build \
  -f "$DOCKERFILE" \
  --build-arg "JAR_FILE=${JAR_FILE}" \
  -t "$FULL_IMAGE" \
  .

echo "Built image: $FULL_IMAGE"

if [[ "$PUSH_IMAGE" == true ]]; then
  echo "[3/3] Pushing image"
  docker push "$FULL_IMAGE"
fi

if [[ "$SAVE_IMAGE_ARCHIVE" == true ]]; then
  if [[ -z "$ARCHIVE_FILE" ]]; then
    SAFE_IMAGE_NAME="${IMAGE_NAME//\//_}"
    SAFE_IMAGE_TAG="${IMAGE_TAG//\//_}"
    ARCHIVE_FILE="${SAFE_IMAGE_NAME}_${SAFE_IMAGE_TAG}.tgz"
  fi

  echo "[3/3] Saving compressed image archive: $ARCHIVE_FILE"
  docker save "$FULL_IMAGE" | gzip > "$ARCHIVE_FILE"
  echo "Saved archive: $ARCHIVE_FILE"
fi

echo "Done"
