# For ease of use with arthas, the agent also uses jdk instead of jre
FROM adoptopenjdk:8-jdk-hotspot
MAINTAINER tengjiqi@gmail.com

ENV APP_NAME=powerjob-worker-agent
# Pass SpringBoot startup parameters and JVM parameters
ENV PARAMS=""
ENV JVMOPTIONS=""

# Install python2.7 by default
RUN apt-get update && \
    apt-get install -y python \
    && apt-get clean \
    && apt-get autoclean \
    && rm -rf /var/lib/apt/lists/*
# Install the wait-for-it script
RUN curl -o wait-for-it.sh https://gitee.com/KFCFans/wait-for-it/raw/master/wait-for-it.sh
RUN chmod +x wait-for-it.sh
# copy main file
COPY powerjob-agent.jar /powerjob-agent.jar
# exposed port（AKKA-Client）
EXPOSE 27777
# Mount the data volume and output the file directly to the host (note that the anonymous volume is mounted here, that is, the location of the host is random)
VOLUME /root
# start application
ENTRYPOINT ["sh","-c","java $JVMOPTIONS -jar /powerjob-agent.jar $PARAMS"]