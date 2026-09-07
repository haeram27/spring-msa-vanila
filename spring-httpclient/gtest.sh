#!/usr/bin/env bash
: ${TESTNAME:="HelloTests.hello"}
[[ -n $1 ]] && TESTNAME=$1
gradle test ${GRADLE_OPT} --rerun-tasks --tests "${TESTNAME}"
