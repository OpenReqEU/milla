#!/bin/sh
# author Mikko Raatikainen mikko.raatikainen@helsinki.fi December 18, 2018
echo "***************************************************************************************************************************************************************"
echo "This is a script that fetches Qt Jira issues from different projects, stores the issues to mallikas, detects dependencies and updates the transitive closure."
echo "A copy (cache) of Jira data is made in the database in order to make data processing faster and change the data to OpenReq JSON format. The database is not persistent."
echo "This script can be used after rebooting the services in the infra."
echo ""
echo "Note that you can get a local copy of the data in OpenReq JSON format using the following command after this script has been executed. Replace QTWB with the desired project name" 
echo "curl -X POST --header 'Content-Type: application/json' --header 'Accept: text/plain' -d 'QTWB' 'http://217.172.12.199:9203/requirementsInProject' > YOURFILE.json"
echo "***************************************************************************************************************************************************************"

## Potential projects roughly in the order of size.
## Note that the two largest projects can take tens of minutes depending on network speed and load of the computer.
# QTWB
# QTSYSADM
# QTJIRA
# AUTOSUITE
# PYSIDE
# QBS
# QTQAINFRA
# QT3DS
# QTCREATORBUG
# QTBUG

# The projects array. EDIT HERE THE DESIRED PROJECTS
#
# For testing this script, use for simplicity only QTWB, which is the smallest project.
projects=("QTWB" "QBS" "QTCREATORBUG" "QTBUG")


echo -e "\nThis script uploads the following projects. To change the projects, change the projects array in the script."
for i in "${projects[@]}"
do
	echo "$i"
done

echo -e "\nData uploading starts:" 
date

for i in "${projects[@]}"
do
	# # 1)
	# # Get data from Jira to Mallikas
	# echo -e "\nUploading $i from Jira to Mallikas"
	# start=`date +%s`
	# curl -X POST --header 'Content-Type: application/json' --header 'Accept: text/plain' -d $i 'http://217.172.12.199:9203/qtjira' 
	# end=`date +%s`
	# echo -e "\nRuntime = $((end-start)) seconds"
	
	# # 2)
	# # THIS IS NOW OUT-COMMENTED BECAUSE ACCIDENTIALLY DOING THIS FOR QTBUG OR QTCREATORBUG MIGHT BE TOO EXPENSIVE COMPUTATIONALLY TAKING EVEN DAYS TO COMPUTE.
	# echo -e "\nSimilarity detection is now out commented and disabled."
	# # Detect dependencies using UPC similarity detection
	# start=`date +%s`
	# echo "Detecting similarity for $i using UPC similarity detection"
	# curl -X POST --header 'Content-Type: application/json' --header 'Accept: */*' 'http://217.172.12.199:9203/detectSimilarityProject?compare=Name-Text&component=DKPro&elements=1000&projectId='$i'&threshold=0.3'	
	# end=`date +%s`
	# echo "Runtime = $((end-start)) seconds"
	
	
	# # 3)
	# Post to Mulperi and KeljuCaaS to construct a transitive closure
	echo -e "\nPosting $i to Mulperi and Kelju to construct a transitive closure"	
	start=`date +%s`
	curl -X POST --header 'Content-Type: application/json' --header 'Accept: text/plain' -d $i  'http://217.172.12.199:9203/sendProjectToMulperi' 
	end=`date +%s`
	echo -e "Runtime = $((end-start)) seconds"

done


echo -e "\nScript ends:" 
date