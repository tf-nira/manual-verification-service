# Manual-Verification-Service

This repository contains the source code and design documents for NIRA Manual Verification Server. For an overview refer here. The modules exposes API endpoints. For a reference front-end UI implementation refer to Manual Verification UI github repo

The Manual Verification Serice Module consists of only one service (Manula Verification Service)
Manual Verification Service is a spring boot housed service designed to assign applications to officers based on the role mapping of the officer.

DB: mosip_mvs  
Tables:  
mvs_application
mvs_application_h
officer_assignment

Config-Server
To run Manual Verification Service, run Config Server

Build & run (for developers)

Prerequisites:
1. create the Roles and assign users to the roles using keycloak. (take into account if any attribute is added for the user e.g. District Officer should have an attribute
   district mapped with the user) 
Following are the default roles for manual-verification-service:
(Officer, Supervisor, District Officer, Legal Officer, International Officer, Senior Registration Officer, Executive Director)  

2.Config Server  
3.JDK 1.11  

Build and install:
$ cd kernel
$ mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
Build Docker for a service:
$ cd <service folder>
$ docker build -f Dockerfile
Deploy
To deploy Commons services on Kubernetes cluster using Dockers refer to Sandbox Deployment.
