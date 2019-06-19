#!/bin/sh

projects=("QTPLAYGROUND"  "QTWB"  "QTSOLBUG"  "QTSYSADM"  "QTJIRA"  "QSR"  "QDS"  "QTVSADDINBUG"  "QTWEBSITE"  "AUTOSUITE" "PYSIDE"  "QTCOMPONENTS"  "QTIFW"  "QBS"  "QTMOBILITY"  "QTQAINFRA"  "QT3DS" "QTCREATORBUG" "QTBUG")

URL=localhost
#URL="217.172.12.199"


#Get data from Jira to Mallikas
for i in "${projects[@]}"
do
	echo -e "\nUploading $i from Jira to Mallikas"
	start=`date +%s%N`
	curl -X POST --header 'Content-Type: application/json' --header 'Accept: text/plain' -d $i 'http://'$URL':9203/qtJira' 
	end=`date +%s%N`
	echo -e "\nRuntime = $(((end-start)/1000000)) milliseconds"
done
