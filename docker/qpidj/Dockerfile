# Qpid java broker
FROM java:8-alpine

RUN apk update && apk add bash

ENV QPID_VERSION=7.1.0

RUN cd /usr/local && \
    wget http://archive.apache.org/dist/qpid/broker-j/${QPID_VERSION}/binaries/apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz && \
    tar -xvf apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz && \
    ln -s qpid-broker/${QPID_VERSION} qpid && \
    rm -f apache-qpid-broker-j-${QPID_VERSION}-bin.tar.gz

ENV QPID_WORK=/work

ADD config.json /work/
ADD entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]