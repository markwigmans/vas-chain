# Ximedes SVA Challenge on the Blockchain
This proof of concept project is inspired by the [Ximedes SVA Challenge](https://www.ximedes.com/ximedes-virtual-account-challenge/) and described in this [blog](https://www.ximedes.com/sva-challenge-a-la-blockchain/).

## Installation
To install the application, perform the following steps:

1. Install [java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. Install [Apache Maven](https://maven.apache.org)
3. Install [Chain Core](https://chain.com)
4. Start ```Chain Core``` and create / connect to a blockchain
5. run ```mvn spring-boot:run```

The application is now started.

## Tests
for testing purposes, [gatling](http://gatling.io) tests are created. To see the actual application working, perform the following steps:

1. update file (if needed) ```src/test/scala/vas/chain/Config.scala```
2. run ```testall.sh```
