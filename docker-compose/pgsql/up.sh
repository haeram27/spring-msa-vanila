#!/usr/bin/env bash
set -u


# ======================
# SCRIPT COMMON
# ====================== 

# syntax) echo_log "my messages"
LOG_FILE=/dev/null
R='\033[0;31m'  # RED
G='\033[0;32m'  # GREEN
N='\033[0m'     # NORMAL (NO COLOR)

echo_log() {
  echo -e "$(date --rfc-3339=seconds) $1" | tee -a ${LOG_FILE}
}

echo_success() {
  echo_log "${G}[SUCCESS]${N} $1"
}

echo_failed() {
  echo_log "${R}[FAILED]${N} $1"
}

echo_fatal() {
  echo_log "${R}[FATAL]${N} $1"
  exit 1
}

check_root_euid() {
  if [[ ${EUID} -ne 0 ]]; then
    echo_log "Please run this script as root"
    exit 1
  fi
}


# ======================
# DOCKER COMMON
# ====================== 

is_container_running() {
  local container=$1
  [[ "$(docker container inspect -f '{{.State.Status}}' ${container})" = "running" ]]
}


stop_container_force() {
  echo_log "[start] ${FUNCNAME} $@"
  local container=$1
  local timeout=60

  echo_log "try to stop container [${container}]"
  docker stop -t ${timeout} "${container}" &>/dev/null
}


# ======================
# CONTAINER SPECIFIC
# ====================== 

is_postgres_cont_available() {
  local container=${POSTGRES_CONTAINER_NAME}

  local postgres_addr=${POSTGRES_ADDRESS:-127.0.0.1}
  local postgres_port=${POSTGRES_PORT:-5432}
  local postgres_db=${POSTGRES_DB_NAME:-postgres}
  local postgres_user=${POSTGRES_USER_NAME:-postgres}
  local postgres_passwd=${POSTGRES_PASSWORD:-postgres}
  local postgres_connect_info="-h ${postgres_addr} -p ${postgres_port} -d ${postgres_db} -U ${postgres_user} "

  if is_container_running ${container}; then
    PGPASSWORD=${postgres_passwd} docker exec ${container} psql ${postgres_connect_info} -c "select 'alive'" &>/dev/null
    if [[ $? -ne 0 ]]; then
      echo_failed ${FUNCNAME}
      return 1
    fi
  fi
}


# ======================
# MAIN
# ====================== 

POSTGRES_CONTAINER_NAME="test-postgres"
POSTGRES_DB_NAME="dvdrental"
POSTGRES_USER_NAME="postgres"
POSTGRES_PASSWORD="postgres"

init_pg_db() {
  local retry_count=1
  local max_retry=${1:-10}
  local sleep_interval=${2:-1}

  while :; do
    if [[ ${retry_count} -gt ${max_retry} ]]; then
      ## TODO: finally failed
      return 1
    fi

    is_postgres_available
    if [[ $? -eq 0 ]]; then
      docker exec ${POSTGRES_CONTAINER_NAME} pg_restore -U ${POSTGRES_USER_NAME} -d ${POSTGRES_DB_NAME} /init/pgsql/sampledb/dvdrental.tar
      if [[ $? -eq 0 ]]; then
      return 0
      fi
    fi

    echo_log "[${retry_count}] retry"
    ((retry_count++))
    sleep ${sleep_interval}
  done
}

docker compose up -d
init_pg_db
