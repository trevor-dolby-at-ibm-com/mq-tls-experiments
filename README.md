# mq-tls-experiments
Temp repo for experiments

```
/opt/jdk-17.0.16+8/bin/java -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.keyStoreType=PKCS12 -Djavax.net.ssl.keyStore=/home/tdolby/github.com/ace-mq-tls-examples/generated-output/mqclient-p12/mqclient-plus-CA1.p12 -Djavax.net.ssl.trustStorePassword=changeit -Djavax.net.ssl.trustStoreType=PKCS12 -Djavax.net.ssl.trustStore=/home/tdolby/github.com/ace-mq-tls-examples/generated-output/mqclient-p12/mqclient-plus-CA1.p12 -jar target/mq-jms-experiment-1.0.0-SNAPSHOT.jar
```
