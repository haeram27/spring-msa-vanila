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


stop_container_grace() {
  echo_log "[start] ${FUNCNAME} $@"

  local container=$1
  local retry_count=0
  local max_retry=60

  if [[ $(is_container_running ${container}) -eq 0 ]]; then
    echo_log "try to stop container by itself: [${container}]"
  
    while :; do
      if [[ ${retry_count} -gt ${max_retry} ]]; then
        echo_fatal "max retries exceeded for graceful shutdown."
      fi
  
      if [[ $((retry_count%4)) -eq 0 ]]; then
        ## TODO: add code to graceful shutdown by application
        #${MONGO_SHELL} 127.0.0.1:${port}/admin --eval "db.shutdownServer()"
      fi
  
      docker container inspect ${container} &>/dev/null
      if [[ $? -ne 0 ]]; then
        echo_success "stop container ${container}"
        break
      fi
  
      echo_log "[${retry_count}] waiting to stop container: ${container}"
      ((retry_count++))
      sleep 3
    done
  fi
}


# ======================
# CONTAINER SPECIFIC
# ====================== 

is_postgres_available() {
  local container=$1
  local db_user=$2
  local db_password=$3
  local db_database=$4

  if [[ $(is_container_running ${container}) -eq 0 ]]; then
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

docker compose up -d
while :; do
  if [[ ${retry_count} -gt ${max_retry} ]]; then
    echo_log "max retries exceeded for graceful shutdown."
    echo_log "forced shutdown is progressing."
    docker kill ${mongo_cont_prefix}-${member}
    exit 1
  fi
  
  if [[ $((retry_count%4)) -eq 0 ]]; then
    ${MONGO_SHELL} 127.0.0.1:${port}/admin --eval "db.shutdownServer()"
  fi
  
  docker container inspect ${mongo_cont_prefix}-${member} &>/dev/null
  if [[ $? -ne 0 ]]; then
    echo_success "stop container ${mongo_cont_prefix}-${member}"
    break
  fi
  
  echo_log "[${retry_count}] waiting to stop container: ${mongo_cont_prefix}-${member}"
  ((retry_count++))
  sleep 3
done

docker compose exec test-postgres pg_restore -U postgres -d dvdrental /sampledb/dvdrental.tar
