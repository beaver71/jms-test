FROM openjdk:8-jre-alpine

ENV ARTEMIS_VERSION 2.7.0
ENV ARTEMIS apache-artemis-$ARTEMIS_VERSION
ENV SHA512_VAL=9ccc9bcbfdbe1d7a30988089acb2751e24bf60fa322a6b27ce420baafc1fe189902460e7bb94ad8a7f4ee286ac27024c5d7b4a95f70547070c0c77f09b5319f7

ENV ARTEMIS_HOME /opt/artemis

RUN set -x && \
    mkdir -p /opt && \
    apk --update add --virtual build-dependencies curl && \
    curl https://www.apache.org/dist/activemq/activemq-artemis/$ARTEMIS_VERSION/$ARTEMIS-bin.tar.gz -o $ARTEMIS-bin.tar.gz

# Validate checksum
RUN if [ "$SHA512_VAL" != "$(sha512sum $ARTEMIS-bin.tar.gz | awk '{print($1)}')" ];\
    then \
        echo "sha512 values doesn't match! exiting."  && \
        exit 1; \
    fi;

RUN tar xzf $ARTEMIS-bin.tar.gz -C  /opt && \
    ln -s /opt/$ARTEMIS $ARTEMIS_HOME && \
    apk del build-dependencies && \
    rm -rf /var/cache/apk/*

WORKDIR $ARTEMIS_HOME
	
RUN ./bin/artemis create /opt/artemis_broker --user guest --password guest --allow-anonymous --http-port 8162
	
ADD broker.xml /opt/artemis_broker/etc/
ADD bootstrap.xml /opt/artemis_broker/etc/
ADD jolokia-access.xml /opt/artemis_broker/etc/

USER root

WORKDIR /opt/artemis_broker

CMD ["sh", "-c", "/opt/artemis_broker/bin/artemis run"]
