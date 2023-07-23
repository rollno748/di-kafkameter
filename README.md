# DI-KafkaMeter

## Introduction

This plugin is an extended version of Kafkameter with restructured UI and additional features.

## Required Components

1. Apache Jmeter
2. Kafka connection configs

## Jar Dependencies Required

* kafka-clients-3.3.1
* guava-31.1-jre
* lz4-java-1.8.0
* snappy-java-1.1.8.4
* zstd-jni-1.5.2-1

## Jmeter Target

* Jmeter version 5.x or above
* Java 8 or above

## What's New?

* Fixed the serializer default value for producer 
* Consumer Support Enhancement
* Provided support for security (Including JAAS support)
* Removed redundant libraries and built as non shaded jar.
* Ability with the consumer to define the no of records to read per request
* support Jmeter5.6.2
* consumer support async commit
* Consumer support muti-thread

## Installation Instructions

* Download the source code from the GitHub.
* Just do a mvn clean install (M2 is required)
* Jar will be generated under the target directory (di-kafkameter-1.3.jar).
* Copy the Jar to \<Jmeter Installed Directory\>/lib/ext/

## Config Elements

* Producer Config Element - [wiki](https://github.com/rollno748/di-kafkameter/wiki#producer-elements)
* Consumer Config Element - [wiki](https://github.com/rollno748/di-kafkameter/wiki#consumer-elements)

## Config Properties for Config Element.

Please refer the Wiki for the list of properties - which can be used

* Producer config properties - [wiki](https://github.com/rollno748/di-kafkameter/wiki#producer-properties)
* Consumer config properties - [wiki](https://github.com/rollno748/di-kafkameter/wiki#consumer-properties)

## Credits
A big thanks to the [Instaclustr](https://www.instaclustr.com/) for providing free tier, which really helped to validate this plugin on kafka.

Kudos to the [Instaclustr](https://www.instaclustr.com/) team to make it easier for the end user to spin up the cluster in no time.

## References

* Kafka Docs: https://kafka.apache.org/documentation/
* Plugin Overview: https://github.com/BrightTag/kafkameter
* Adding headers to kafka message: https://stackoverflow.com/questions/29025627/adding-custom-headers-in-kafka-message


## 💲 Support Me
If this project help you reduce time to develop, you can give me a cup of coffee :)

![](img.png)

Please open up a :beetle: - If you experienced something.
