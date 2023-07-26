#!/usr/bin/env bash
export JAVA_PROGRAM_ARGS=`echo "$@"`
mvn exec:java -Dexec.mainClass="com.personal.token.API" -Dexec.cleanupDaemonThreads=false -Dexec.args="$JAVA_PROGRAM_ARGS" 