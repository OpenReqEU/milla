#!/bin/sh
# author Mikko Raatikainen mikko.raatikainen@helsinki.fi March 6, 2019
#
# This script loops through a set of issue IDs stored in OpenReq infra (Milla/Mallikas) from Qt Jira and 
# checks the their consistency over max five steps of transitive relations.
#


# Regexp all IDs
projectIDstring=`curl -s -X POST --header 'Content-Type: application/json' --header 'Accept: text/plain' -d 'QBS' 'http://localhost:9203/requirementsInProject' | grep -o -P "\"id\":\"[A-Z][A-Z]*-[0-9][0-9]*\"" | grep -o -P "[A-Z][A-Z]*-[0-9][0-9]*"`

# Make an array of IDs for looping
projectID=($(echo $projectIDstring))

# Print the number of items
# echo -e "Start consistency check for ${#projectID[@]} items" 

# Save the start time
start=`date +%s`

# Loop through all IDs to check consistency for all of them
for i in "${projectID[@]}"
do
	JSONresp=`curl -s -X POST --header 'Content-Type: application/json' --header 'Accept: text/plain' 'http://localhost:9203/getConsistencyCheckForRequirement?requirementId='$i`
	# Does it result in a diagnosis part? If yes, it's incosistent
	if [[ $JSONresp == *"\"AnalysisVersion\": \"reqreldiag\""* ]]; then
		echo -e $i"\tfalse\n"
	else
		echo -e $i"\ttrue\n"
	fi
done

# Save and print the time that consistency check took. This excludes parsing or reqexping the IDs.
end=`date +%s`
echo -e "\nRuntime of consistency check: $((end-start)) seconds"

