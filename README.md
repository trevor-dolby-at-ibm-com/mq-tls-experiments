# mq-tls-experiments

Experiments with MQ TLS clients using Spring Boot.

## Overview

This repo builds on top of the TLS keys and certificates created by the 
https://github.com/trevor-dolby-at-ibm-com/ace-mq-tls-examples repo, including
the [OpenShift](https://github.com/trevor-dolby-at-ibm-com/ace-mq-tls-examples/tree/main/openshift)
variant that uses CP4i queue managers.

Spring Boot applications rely on properties files for a lot of the configuration
as well as using the keystores with the client and server certificates:

![mq-jms-cp4i-light](/pictures/mq-jms-cp4i-light.png#gh-light-mode-only)![mq-jms-cp4i-dark](/pictures/mq-jms-cp4i-dark.png#gh-dark-mode-only)

The applications and configuration in this repo also borrow from two other sources:

- The MQ JMS Spring Components at https://github.com/ibm-messaging/mq-jms-spring
- The CICS Spring examples from https://github.com/cicsdev/cics-java-liberty-springboot-jms

## Usage

Configuration of the queue manager is beyond the scope of this repo, and this documentation
is written assuming something similar to https://github.com/trevor-dolby-at-ibm-com/ace-mq-tls-examples/tree/main/openshift
using the definitions shown at https://github.com/trevor-dolby-at-ibm-com/ace-mq-tls-examples/blob/main/openshift/mq-tls-configmap.yaml

The 2.7.0 and 4.0.5 directories contain the same application that puts a message to
`DEMO.QUEUE` when called via HTTP. The configuration in the application.properties
files is likely to need changing, with the channel, host, queue manager, etc needing
to be modified. The certificates will need to be created, with the 
[ACE TLS examples repo](https://github.com/trevor-dolby-at-ibm-com/ace-mq-tls-examples)
being one way (but not the only way) to create the various certificates.

Once the configuration has been modified, the build process in the subdirectories uses Maven:
```bash
mvn package
```
and then the resulting code can be run with a command like
```bash
java -jar target/mq-jms-experiment-1.0.0-SNAPSHOT.jar
```
with curl used to trigger a message put:
```bash
curl 'http://localhost:8080/send/DEMO.QUEUE?data=hello'
```

#### Notes for 2.7.0

For 2.7.0, it is likely that the command will need to specify the keystore using system properties:
```bash
java -Djavax.net.ssl.keyStorePassword=changeit \
     -Djavax.net.ssl.keyStoreType=PKCS12 \
     -Djavax.net.ssl.keyStore=/home/tdolby/github.com/ace-mq-tls-examples/generated-output/mqclient-p12/mqclient-plus-CA1.p12 \
     -Djavax.net.ssl.trustStorePassword=changeit \
     -Djavax.net.ssl.trustStoreType=PKCS12 \
     -Djavax.net.ssl.trustStore=/home/tdolby/github.com/ace-mq-tls-examples/generated-output/mqclient-p12/mqclient-plus-CA1.p12 \
     -jar target/mq-jms-experiment-1.0.0-SNAPSHOT.jar
```
The only time this would not be needed is if the MQ channel does not require client authentication
(so no need for a keystore) and the queue manager is using a certificate that is issued by one of
the standard CAs that is already trusted by the JVM (so no need for a truststore). The configuration
tested for this repo meets neither of those conditions (the channel has `SSLCAUTH(REQUIRED)` and the
certificates come from a private CA) so omitting the system properties will lead to a 2397 MQRC_JSSE_ERROR.

Both JKS and PKCS12 files work with modern Java releases, and after 2.7.14 it is possible to use
the `ibm.mq.jks` options in application.properties to avoid modifying the JVM system properties. See 
[2.7.14 and later](#2714-and-later) below for details.

#### Notes for 4.0.5

The configuration options from 2.7.0 still work (including the `ibm.mq.jks` options) but in general 
the use of [SSL bundles](https://spring.io/blog/2023/06/07/securing-spring-boot-applications-with-ssl#introducing-ssl-bundles)
with PEM files is preferred. SSL bundles will also work with PKCS12 and JKS files, but the PEM file
approach works better with container secrets that provide the separate files.

## Configuration options

### Initial 2.7.0

From [spring-boot-2.7.0/src/main/resources/application.properties](/spring-boot-2.7.0/src/main/resources/application.properties)
```
ibm.mq.queueManager=cp4iqm
ibm.mq.channel=MQ.CLIENT.SVRCONN
ibm.mq.connName=cp4iqm-ibm-mq-qm-cp4i.apps.openshift.yourcompany.com(443)
ibm.mq.user=
ibm.mq.sslCipherSuite=TLS_AES_128_GCM_SHA256
ibm.mq.sslPeerName=CN=mqserver,OU=ExpertLabs,O=IBM,L=Minneapolis,ST=MN,C=US
ibm.mq.outboundSNI=HOSTNAME
```
JSSE will automatically select a key from the keystore for client authentication
so there is no need in this case to specify the client certificate alias. See
[outboundSNI](#outboundsni) below for details on that setting.

### 2.7.14 and later

From [spring-boot-2.7.0/src/main/resources/application.properties](/spring-boot-2.7.0/src/main/resources/application.properties)
```
ibm.mq.jks.trustStore=/home/tdolby/github.com/ace-mq-tls-examples/generated-output/mqclient-p12/mqclient-plus-CA1.p12
ibm.mq.jks.trustStorePassword=changeit
ibm.mq.jks.keyStore=/home/tdolby/github.com/ace-mq-tls-examples/generated-output/mqclient-p12/mqclient-plus-CA1.p12
ibm.mq.jks.keyStorePassword=changeit
```
These options are now deprecated in favor of SSL bundles, but still work and are the
best way to specify keystores for at version 2.7. Note that PKCS12 files work despite the
"jks" naming convention for the properties.

### Bundles 

The previous styles of configuration still work but the new SSL bundles support is preferred.

From [spring-boot-4.0.5/src/main/resources/application.properties](/spring-boot-4.0.5/src/main/resources/application.properties)
```
ibm.mq.sslBundle=mq-tls-pem-bundle
```
where `mq-tls-pem-bundle` is defined in [spring-boot-4.0.5/src/main/resources/application.yaml](/spring-boot-4.0.5/src/main/resources/application.yaml)
```
spring:
  ssl:
    bundle:
      pem:
        mq-tls-pem-bundle:
          keystore:
            certificate: "file:/home/tdolby/github.com/ace-mq-tls-examples/generated-output/mqclient-keys/mqclient.crt"
            private-key: "file:/home/tdolby/github.com/ace-mq-tls-examples/generated-output/mqclient-keys/mqclient-decrypted.key"
          truststore:
            certificate: "file:/home/tdolby/github.com/ace-mq-tls-examples/generated-output/ace-demo-CA1/ace-demo-CA1.crt"
```
There is also a `mq-tls-jks-bundle` in the same file that uses the PKCS12 keystores.

### outboundSNI

The `ibm.mq.outboundSNI=HOSTNAME` setting tells the MQ client code to use the destination hostname for the
"Server Name Indication" (SNI) header instead of using the channel name. See the description at 
https://github.com/trevor-dolby-at-ibm-com/ace-mq-tls-examples/blob/main/openshift/README.md#tls-sni for
more details, but using "HOSTNAME" is usually best for CP4i queue manager connections.

## JMS connection factory options

The list of string attributes can be found in https://public.dhe.ibm.com/software/integration/wmq/docs/V9.4/PDFs/mq94.develop.pdf
and includes settings such as `sslPeerName` and `sslCipherSuite`. The `outboundSNI` setting is separate from
the connection factory and should be set as a system property:
```
-Dcom.ibm.mq.cfg.SSL.outboundSNI=HOSTNAME
```
Keystores and truststores may also need to be configured as system properties as shown above.

## Debugging

Errors such as `JMSCMQ0001: IBM MQ call failed with compcode '2' ('MQCC_FAILED') reason '2397' ('MQRC_JSSE_ERROR')` 
tend to be quite generic with very little debugging information. Setting
```
-Djavax.net.debug=all
```
on the java command line will cause the JSSE layer to print out large amounts of information, some
of which may be useful in understanding the details of what has gone wrong.

Errors like `JMSCMQ0001: IBM MQ call failed with compcode '2' ('MQCC_FAILED') reason '2035' ('MQRC_NOT_AUTHORIZED')` 
show that the TLS part of the connection process has succeeded, but one or more of the MQ operations
requires more authorization. This can be investigated on the queue manager side by checking the AMQERR01.LOG
file for more information. 

See also https://github.com/trevor-dolby-at-ibm-com/ace-mq-tls-examples/blob/main/openshift/README.md#qm-securitypolicy-set-to-userexternal

