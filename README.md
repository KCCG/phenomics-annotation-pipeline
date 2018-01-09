Introduction 
============
Phenmomic-Annotation-Pipeline is a Java8 SpringBoot Application. The core module (Engine) is the backend processing unit enriched with Standford CoreNLP text processing and custom annotators.

Following are key components for this service:

**Engine**
* Construct structured article with information received from ingestion service.
* Linguistically process article abstract
    * Sentence formation -> Yielding `APSentence` array in Article Abstract.
    * Tokenization -> Yielding `APToken` array in each sentence.
    * Lemmatization -> Storing Lemma and POS in each APToken.
    * Dependency parsing -> Yielding lists of `APDependencyRelation` and `APParseTreeRow` in each sentence.
* Shortform extraction and linkage with long forms.
* Annotation Processing
    * Gene Annotations -> Stored as list of `LexicalEntity` in each sentence.
* Deduplication (Filter based on already fetched articles using DynamoDB Indexing).


**Database Handling**

Being a micro-service pipeline manages its own databases.
There are 3 storage systems used to persist processed Articles.
* Graph Database:
    Neo4J has been chosen (After comparing it with Titan, MongoDB) as graph database storage. Here are a few responsibilities it shares:
    * Storing each Article as a mini-graph of relationships. Important relations are PUBLISHED, WROTE, CONTAINS linking an article with Publications, Authors and Annotations respectively.
    * Providing a quick indexed search for interfacing APIs. Search can be a composite query (Publication parameters like date, author, publication or annotations like gene, diseases etc.)
* DynamoDB:
  Dynamodb stores Article, Annotations and Subscriptions.

* S3:
  S3 is used to store/dump linguistic information of each Article. This decision has been made because of less frequent usage and large object size.

**Interfaces**

There are two interfaces for this service:

1: JAVAFx UI : For internal testing of engine and not being maintained actively.

2: Restful APIs.

***APIs***

There are multiple (still growing) APIs taking care of processing of Articles, Search and Subscription. The search service is pretty young and will be take out as a separate service in version 2.0.

A few notable points:
* There are 3 controllers (`AnnotationController`, `QueryController`, `SubscriptionController`)
* Each controller talks to a manager from engine, it is responsibility of a manger to understand the request and fulfill it.

Setup
=====
1. The build tool is gradle to keep dependencies clean and build process simple.
2. This is a dockerized application. Although DockerFile is provided and works in coordination with `compose` but the process has been updated and gradle is used to produce Docker image. This makes Docker container very lean and no port bindings are given at this stage. This is to facilitate deployment on ECS.
3. To run the application locally `./gradlew bootRun` can be used.
4. Application uses `port 9080` (Just because it is easy to type that number.)

Deployment
==========
1. DevOps code contains script to build docker image, tag it and publish it to ECR.
2. DevOps requires AWS CLI and credentials in environment variables.


Install in Intellij
===================
This project uses Lombok and you need to install the following to get it to work:
* Lombok plugin
* Enable Annotation processing by going to:
Preferences -> Build, execution, deployment, compiler, Annotation Processors
Creating a profile for the project and enabling it

https://www.jetbrains.com/help/idea/2016.1/configuring-annotation-processing.html

Rebuild and then you should be good to go. 

