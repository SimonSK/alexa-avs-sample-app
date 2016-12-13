#!/bin/bash
use_template()
{
  Template_Loc=$1
  Template_Name=$2
  Target_Name=$3
  while IFS='' read -r line || [[ -n "$line" ]]; do
    while [[ "$line" =~ (\$\{[a-zA-Z_][a-zA-Z_0-9]*\}) ]]; do
      LHS=${BASH_REMATCH[1]}
      RHS="$(eval echo "\"$LHS\"")"
      line=${line//$LHS/$RHS}
    done
    echo "$line" >> "$Template_Loc/$Target_Name"
  done < "$Template_Loc/$Template_Name"
}

ProductID=YOUR_PRODUCT_ID
SubscriptionKey="xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
DeviceSerialNumber="1xxxxxxxxxx"
KeyStorePassword=""
Wake_Word_Detection_Enabled="false"

Java_Client_Loc="/home/pi/alexa-avs-sample-app/samples/javaclient"
#if [ -f $Java_Client_Loc/config.json ]; then
#  rm $Java_Client_Loc/config.json
#fi
#use_template $Java_Client_Loc template_config_json config.json

rm ${Java_Client_Loc}/pom.xml 
cp ${Java_Client_Loc}/pom_pi.xml ${Java_Client_Loc}/pom.xml
cd ${Java_Client_Loc} && mvn validate && mvn install
