FROM openjdk:8-jre-alpine

# SET TIMEZONE IN IMAGE
ENV LANG=ru_RU.UTF-8
ENV LANGUAGE ru_RU:ru
ENV LC_ALL ru_RU.UTF-8
ENV TZ=Europe/Moscow

RUN apk add --update --no-cache tzdata && \
    cp /usr/share/zoneinfo/${TZ} /etc/localtime && \
    echo "${TZ}" > /etc/timezone && \
    rm -fr /tmp/* /var/cache/apk/* && \
    apk del tzdata


ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS="-Djava.net.preferIPv6Addresses=true"

CMD java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.war

EXPOSE 8080

ADD *.war /app.war
