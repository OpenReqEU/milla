#!/bin/sh
# author Mikko Raatikainen 

## Potential projects roughly in the order of size.
projects=("QTPLAYGROUND"  "QTWB"  "QTSOLBUG"  "QTSYSADM"  "QTJIRA"  "QSR"  "QDS"  "QTVSADDINBUG"  "QTWEBSITE"  "AUTOSUITE" "PYSIDE"  "QTCOMPONENTS"  "QTIFW"  "QBS"  "QTMOBILITY"  "QTQAINFRA"  "QT3DS" "QTCREATORBUG" "QTBUG")

URL=localhost
#URL="217.172.12.199"


# Post to Mulperi and KeljuCaaS to construct a transitive closure

for i in "${projects[@]}"
do
	echo -e "\nPosting $i to Mulperi and Kelju to construct a transitive closure. "	
	start=`date +%s%N`
	curl -s -X POST --header 'Content-Type: application/json' --header 'Accept: text/plain'  'http://'$URL':9203/sendProjectToMulperi?projectId='$i
	end=`date +%s%N`
	echo -e "\nRuntime = $(((end-start)/1000000)) milliseconds"

done
