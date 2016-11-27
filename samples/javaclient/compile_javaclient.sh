#!/bin/bash

Java_Client_Loc="/home/pi/Desktop/alexa-avs-sample-app/samples/javaclient"
rm ${Java_Client_Loc}/pom.xml 
cp ${Java_Client_Loc}/pom_pi.xml ${Java_Client_Loc}/pom.xml
cd ${Java_Client_Loc} && mvn validate && mvn install
