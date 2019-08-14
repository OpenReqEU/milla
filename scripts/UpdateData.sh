#!/bin/bash

projects=("QTPLAYGROUND"  "QTWB"  "QTSOLBUG"  "QTSYSADM"  "QTJIRA"  "QSR"  "QDS"  "QTVSADDINBUG"  "QTWEBSITE"  "AUTOSUITE" "PYSIDE"  "QTCOMPONENTS"  "QTIFW"  "QBS"  "QTMOBILITY"  "QTQAINFRA"  "QT3DS" "QTCREATORBUG" "QTBUG")
#projects=("QTBUG")
URL=https://api.openreq.eu/milla
#URL="217.172.12.199:9203"
#URL="http://localhost:9203"


#Get data from Jira to Mallikas
for i in "${projects[@]}"
do
	echo -e "\nUpdating $i from Jira to Mallikas"
	start=`date +%s%N`
	curl -k -X POST --header 'Accept: text/plain' $URL'/updateRecentInProject?projectId='$i 

 
	end=`date +%s%N`
	echo -e "\nRuntime = $(((end-start)/1000000)) milliseconds"
done
