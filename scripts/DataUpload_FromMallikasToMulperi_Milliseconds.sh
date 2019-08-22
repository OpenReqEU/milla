#!/bin/bash
# author Mikko Raatikainen 

## Potential projects roughly in the order of size.
projects=("QTPLAYGROUND"  "QTWB"  "QTSOLBUG"  "QTSYSADM"  "QTJIRA"  "QSR"  "QDS"  "QTVSADDINBUG"  "QTWEBSITE"  "AUTOSUITE" "PYSIDE"  "QTCOMPONENTS"  "QTIFW"  "QBS"  "QTMOBILITY"  "QTQAINFRA"  "QT3DS" "QTCREATORBUG" "QTBUG")
#projects=("QTBUG")


#URL="217.172.12.199:9203"
URL="http://localhost:9203"
#URL=https://api.openreq.eu/milla

# Post to Mulperi and KeljuCaaS to construct a transitive closure

for i in "${projects[@]}"
do
	echo -e "\nPosting $i to Mulperi and Kelju to construct a transitive closure. "	
	start=`date +%s%N`
	curl -k -X POST --header 'Accept: text/plain'  $URL'/sendProjectToMulperi?projectId='$i
	end=`date +%s%N`
	echo -e "\nRuntime = $(((end-start)/1000000)) milliseconds"

done
