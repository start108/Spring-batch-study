FROM openjdk:17

ADD ./build/libs/*.jar appBatch.jar

ADD ./batchRun.sh batchRun.sh

