# DI-KafkaMeter

## Introduction

This plugin is an extended version of Kafkameter with restructured UI and additional features which kafkameter missed

## Required Components

1. DI-Jmeter or Apache Jmeter
2. Kafka Configs (Broker lists, Topic(s), etc)


## Jar Dependencies Required

* kafka-clients-3.3.1
* guava-31.1-jre
* lz4-java-1.8.0
* snappy-java-1.1.8.4
* zstd-jni-1.5.2-1

## Jmeter Target

* Jmeter version 5.x or above
* Java 8 or above

## Installation Instructions

* Download the source code from the Gitlab.
* Just do a mvn clean install (M2 is required)
* Jar will be generated under the target directory (di-kafkameter-1.1.jar).
* Copy the Jar to \<Jmeter Installed Directory\>/lib/ext/ for DI Jmeter \<Jmeter Installed Directory\>/di/plugins

## Config Properties for Config Element.

Please refer the Wiki for the list of properties - which can be used

* Producer config properties - [wiki](https://github.com/rollno748/di-kafkameter/wiki#producer-properties)
* Consumer config properties - [wiki](https://github.com/rollno748/di-kafkameter/wiki#consumer-properties)

## Config Elements

* Producer Config Element - [wiki](https://github.com/rollno748/di-kafkameter/wiki#producer-elements)
* Consumer Config Element - [wiki](https://github.com/rollno748/di-kafkameter/wiki#consumer-elements)

## Credits
A big thanks to the [Instaclustr](https://www.instaclustr.com/) for providing free tier. which really helped me to validate this on kafka.
It's a great effort by the team to make it easy for the end user to spin the kafka cluster in no time.

## References

 * Plugin Overview: https://github.com/BrightTag/kafkameter  
 * How to add headers to message: https://stackoverflow.com/questions/29025627/adding-custom-headers-in-kafka-message
 * TableEditor : https://www.programcreek.com/java-api-examples/?api=org.apache.jmeter.testbeans.gui.TableEditor
 * Validation : Kafka tool (http://www.kafkatool.com/)
 * Producer Config property : https://kafka.apache.org/documentation/#producerconfigs


## 💲 Support Me
[<a href="https://www.buymeacoffee.com/rollno748" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" height="45px" width="162px" alt="Buy Me A Coffee"></a>](https://www.buymeacoffee.com/rollno748)

Please rate a :star2: if you like it.

Please open up a :beetle: - If you experienced something.
 
