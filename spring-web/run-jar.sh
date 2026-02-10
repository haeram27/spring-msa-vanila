#!/usr/bin/env bash

set -x

PID=0
JAR_PATH='build/libs/springwebex-0.0.1-SNAPSHOT.jar'

terminate()
{
    if [ ${PID} -ne 0 ]; then
        kill -SIGTERM ${PID}
        wait ${PID}
    fi
    exit 143
}

trap 'terminate' SIGTERM

# Java Heap options
HEAP_OPTS="-Xms64m -Xmx512m"

# run jar as background
java -jar -XX:+UseG1GC ${HEAP_OPTS} ${JAR_PATH} &
PID=$!

# wait java proc
wait ${PID}

## kill server
# ps -ef | grep 'run-jar.sh'
# 501352  444454  0 17:29 pts/6    00:00:00 bash ./run-jar.sh
# kill 501352