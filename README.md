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

* Consumer Support Enhancement
* Provided support for security (Including JAAS support)
* Removed redundant libraries and built as non shaded jar.
* Ability with the consumer to define the no of records to read per request

## Installation Instructions

* Download the source code from the Github.
* Just do a mvn clean install (M2 is required)
* Jar will be generated under the target directory (di-kafkameter-1.1.jar).
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
* TableEditor : https://www.programcreek.com/java-api-examples/?api=org.apache.jmeter.testbeans.gui.TableEditor


## 💲 Support Me
<!-- [<a href="https://www.buymeacoffee.com/rollno748" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" height="45px" width="162px" alt="Buy Me A Coffee"></a>](https://www.buymeacoffee.com/rollno748) -->
If this project help you reduce time to develop, you can give me a cup of coffee :) 

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://ko-fi.com/rollno748)

Please rate a :star2: if you like it / benefits you.

Please open up a :beetle: - If you experienced something.
