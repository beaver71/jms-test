JMS test
====

### License
See full MIT license text [here](license.md).

### Introduction
[TBD]



## Instructions

### Running brokers
- run `docker-compose up --build` to start brokers 
- run `netstat -tulpn | grep docker` to check services:

```
tcp6       0      0 :::5673                 :::*                    LISTEN      14604/docker-proxy
tcp6       0      0 :::5674                 :::*                    LISTEN      14627/docker-proxy
tcp6       0      0 :::5675                 :::*                    LISTEN      14674/docker-proxy
tcp6       0      0 :::8080                 :::*                    LISTEN      14657/docker-proxy
tcp6       0      0 :::8161                 :::*                    LISTEN      14593/docker-proxy
```

**ActiveMQ** is listening for AMQP connections on **5673** port, and its web console is available at [http://localhost:8161](http://localhost:8161/) (credentials guest/guest)

**Qpid cpp broker** is listening for AMQP connections on **5674** port. No web console is available, but you can use qpid-config via exec docker command, for example run `docker exec -it qpidc qpid-config exchanges -b 127.0.0.1:5674` to see all exchange nodes configured for qpid cpp broker.

See <https://qpid.apache.org/releases/qpid-cpp-1.39.0/cpp-broker/book/chapter-Managing-CPP-Broker.html>

**Qpid java broker** is listening for AMQP connections on **5675** port, web console is at <http://localhost:8080/>  (credentials admin/admin)

### Compiling java testing clients

- run `mvn clean package dependency:copy-dependencies -DincludeScope=runtime -DskipTests`

- run `mvn compile` 

### Running JMS filter testing clients

- run `./startclient.sh Receiver -p 5673` to start a **Receiver** client, connecting to amqp://localhost:5673 and node **croads**, using JMSfilter: `nat='it' AND prod='a22' AND geo LIKE 'u0j2%'` and expecting to consume 10 messages.
- run `./startclient.sh Sender -p 5673` to start a **Sender** client, which connecs to amqp://localhost:5673 and send 5 messages to **croads** node:

```
1-msg: 

body: test1
application_properties:  nat=it, geo=u0j2ws2, det=denm, JMSXDeliveryCount=1, prod=a22, type=asn1

2-msg: 

body: test2
application_properties:  nat=at, geo=, det=ivim, JMSXDeliveryCount=1, prod=xyz, type=datex

3-msg: 

body: test3
application_properties:  nat=at, geo=, det=denm, JMSXDeliveryCount=1, prod=xyz, type=asn1

4-msg: 

body: test4
application_properties:  nat=it, geo=u0j2x5z, det=ivim, JMSXDeliveryCount=1, prod=a22, type=asn1

5-msg: 

body: test5
application_properties: nat=it, geo=u0j8rkm, det=denm, JMSXDeliveryCount=1, prod=a22, type=asn1
```

- it is expected the **Receiver** reports 2 messages received, test1 and test5:

```
1-message: test1
  nat=it, geo=u0j2ws2, det=denm, JMSXDeliveryCount=1, prod=a22, type=asn1
  
2-message: test4
  nat=it, geo=u0j2x5z, det=ivim, JMSXDeliveryCount=1, prod=a22, type=asn1
```



### Running JMS topic testing clients

- [TBD]
- run `./star