JMS test
====

## License
See full MIT license text [here](license.md).

## Introduction
This repo provides:

- Java clients, both Receiver and Sender, used to test **JMS filtering** and **JMS topic** features with different AMQP 1.0 brokers
- Docker multicontainer environment to expose AMQP broker services on different ports

## Instructions

### Running brokers
- run `docker-compose up --build` to start brokers in different containers 
- run `netstat -tulpn | grep docker` to check services:

```
tcp6       0      0 :::5672                 :::*                    LISTEN      16323/docker-proxy
tcp6       0      0 :::5673                 :::*                    LISTEN      16276/docker-proxy
tcp6       0      0 :::5674                 :::*                    LISTEN      16242/docker-proxy
tcp6       0      0 :::5675                 :::*                    LISTEN      16166/docker-proxy
tcp6       0      0 :::5676                 :::*                    LISTEN      16291/docker-proxy
tcp6       0      0 :::8080                 :::*                    LISTEN      16127/docker-proxy
tcp6       0      0 :::15672                :::*                    LISTEN      16253/docker-proxy
tcp6       0      0 :::8161                 :::*                    LISTEN      16191/docker-proxy
tcp6       0      0 :::8162                 :::*                    LISTEN      16218/docker-proxy

```

- **ActiveMQ** is listening for AMQP connections on **5673** port, and its web console is available at [http://localhost:8161/](http://localhost:8161/) (credentials guest/guest)
- **Qpid cpp broker** is listening for AMQP connections on **5674** port. No web console is available, but you can use qpid-config via exec docker command, for example run `docker exec -it qpidc qpid-config exchanges -b 127.0.0.1:5674` to see all exchange nodes configured for qpid cpp broker. See also<https://qpid.apache.org/releases/qpid-cpp-1.39.0/cpp-broker/book/chapter-Managing-CPP-Broker.html>
- **Qpid java broker** is listening for AMQP connections on **5675** port, web console is at <http://localhost:8080/>  (credentials admin/admin)

- **ActiveMQ Artemis** is listening for AMQP connections on **5676** port, and its web console is available at [http://localhost:8162/](http://localhost:8162/) (credentials guest/guest)
- **RabbitMQ (with plugin AMQP 1.0)** is listening for AMQP connections on **5672** port, and its web console is available at [http://localhost:15672/](http://localhost:15672/) (credentials test/test)

### Compiling java testing clients

- run `mvn clean package dependency:copy-dependencies -DincludeScope=runtime -DskipTests`

- run `mvn compile` 

### Running JMS filter testing clients

- run `./startclient.sh Receiver -p 5673` to start a **Receiver** client, connecting to [amqp://localhost:5673](amqp://localhost:5673) and destination node **croads**, using JMSfilter: `nat='it' AND prod='a22' AND geo LIKE 'u0j2%'` and expecting to consume up to 10 messages.
- run `./startclient.sh Sender -p 5673` to start a **Sender** client, which connects to [amqp://localhost:5673](amqp://localhost:5673) and sends 5 messages to destination node **croads** :

> **1-msg:** 
>
> body: test1
> application_properties:  nat=it, geo=u0j2ws2, det=denm, JMSXDeliveryCount=1, prod=a22, type=asn1
>
> **2-msg:** 
>
> body: test2
> application_properties:  nat=at, geo=, det=ivim, JMSXDeliveryCount=1, prod=xyz, type=datex
>
> **3-msg:** 
>
> body: test3
> application_properties:  nat=at, geo=, det=denm, JMSXDeliveryCount=1, prod=xyz, type=asn1
>
> **4-msg:** 
>
> body: test4
> application_properties:  nat=it, geo=u0j2x5z, det=ivim, JMSXDeliveryCount=1, prod=a22, type=asn1
>
> **5-msg:** 
>
> body: test5
> application_properties: nat=it, geo=u0j8rkm, det=denm, JMSXDeliveryCount=1, prod=a22, type=asn1

- it is expected the **Receiver** reports 2 messages received, test1 and test4:

> 1-message: test1
>   nat=it, geo=u0j2ws2, det=denm, JMSXDeliveryCount=1, prod=a22, type=asn1
>
> 2-message: test4
>   nat=it, geo=u0j2x5z, det=ivim, JMSXDeliveryCount=1, prod=a22, type=asn1



*Windows tip*: use `startclient.bat` instead of `startclient.sh`

### Running JMS topic testing clients

- run `./startclient.sh TopicReceiver -p 5673` to start a **Receiver** client, connecting to [amqp://localhost:5673](amqp://localhost:5673) and destination **croads/it.a22.\*.\*.u0j2.#**, that is topic exchange croads and binding key : `it.a22.*.*.u0j2.#` and expecting to consume up to 10 messages.
- run `./startclient.sh TopicSender -p 5673` to start a **Sender** client, which connects to [amqp://localhost:5673](amqp://localhost:5673) and sends 5 messages to destination node **croads** with different topics:

> **1-msg:** 
>
> body: test1, topic: croads/it.a22.asn1.denm.u0j2.w.s.2
>
> **2-msg:** 
>
> body: test2, topic: croads/at.xyz.datex.ivim.
>
> **3-msg:** 
>
> body: test3, topic: croads/at.xyz.asn1.denm.
>
> **4-msg:** 
>
> body: test4, topic: croads/it.a22.asn1.ivim.u0j2.x.5.z
>
> **5-msg:** 
>
> body: test5, topic: croads/it.a22.asn1.denm.u0j8.r.k.m

- it is expected the **Receiver** reports 2 messages received matching the binding key, test1 and test4:

> 1-message: test1
>   nat=it, geo=u0j2ws2, det=denm, JMSXDeliveryCount=1, prod=a22, type=asn1
>
> 2-message: test4
>   nat=it, geo=u0j2x5z, det=ivim, JMSXDeliveryCount=1, prod=a22, type=asn1



## Links:

[Apache ActiveMQ site web](http://activemq.apache.org/)

[Apache Qpid broker C++](http://qpid.apache.org/components/cpp-broker/index.html)

[Apache Qpid broker J](http://qpid.apache.org/components/broker-j/index.html)

[Apache ActiveMQ Artemis](http://activemq.apache.org/artemis/)

[RabbitMQ](https://www.rabbitmq.com/)

[ Java Message Service (JMS) API reference](https://docs.oracle.com/javaee/7/api/index.html?javax/jms/package-summary.html)