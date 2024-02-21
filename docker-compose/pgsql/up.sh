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

is_postgres_available() {
  local container=$1
  local db_user=$2
  local db_password=$3
  local db_database=$4

  if is_container_running ${container}; then
    PGPASSWORD=${db_password} docker exec ${container} psql -U ${db_user} -d ${db_database} -c "select 'alive'" &>/dev/null
    if [[ $? -ne 0 ]]; then
      echo_failed ${FUNCNAME}
      return 1
    fi
  fi
}


# ======================
# MAIN
# ====================== 

CONTAINER_NAME="test-postgres"
PG_USER="postgres"
PG_PWD="postgres"
PG_DB="dvdrental"

init_pg_db() {
  local retry_count=1
  local max_retry=${1:-10}
  local sleep_interval=${2:-1}

  while :; do
    if [[ ${retry_count} -gt ${max_retry} ]]; then
      ## TODO: finally failed
      return 1
    fi

    is_postgres_available ${CONTAINER_NAME} ${PG_USER} ${PG_PWD} ${PG_DB}
    if [[ $? -eq 0 ]]; then
      docker exec ${CONTAINER_NAME} pg_restore -U ${PG_USER} -d ${PG_DB} /init/pgsql/sampledb/dvdrental.tar
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
