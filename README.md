# Milla

Milla is a service in the dependency engine of OpenReq infrastructure that primarily focuses on the contexts, which already contain a large number of existing and dependent requirements, such as large distributed open source projects or large systems engineering projects. For example, the Qt Company has about one hundred-thousand (100,000) issues in its Jira. The dependency engine focuses on the entire body of requirements as an interdepedent "requirements model".

This service was created as a result of the OpenReq project funded by the European Union Horizon 2020 Research and Innovation programme under grant agreement No 732463.

# Technical Description

Milla is an orchestrator (broker) service used in the Qt Jira trial of OpenReq. For further details, see the [swagger documentation](https://api.openreq.eu/milla/swagger-ui.html).

## The following technologies are used:
- Java
- Spring Boot
- Maven
- GSON
	
## Public APIs

The API is documented by using Swagger2: https://api.openreq.eu/#/services/milla

## How to Install

Run the compiled jar file, e.g., `java -jar Milla-1.13.jar`.

Milla uses port 9203 that needs to be open to in order that the Swagger page can be accessed. Milla also connects to Qt Jira ja the Mallikas and Mulperi services of OpenReq.

Milla requires at least Mallikas service to be running.

For token-based authorization using headers, the token can be added as a run property --milla.authToken=“tokenhere”, or 
in the appropriate application.properties file. 
If no token is provided, no such header will be included in the requests.

## How to Use This Microservice

The swagger page describes all endpoints. Below are examples of the key commands for command line usage:

Milla fetches the relevant data from Qt Jira and stores (caches) the data to Mallikas' database to be more efficiently accessible and in order to be in OpenReq JSON format. qtjira endpoint is used with the project as a parameter.

`curl -X POST --header 'Content-Type: application/json' --header 'Accept: text/plain' -d 'QTWB' 'https://api.openreq.eu/milla/qtJira'`

A copy of the data in OpenReq JSON can be fetched  using the following command after the project has been stored in Mallikas (see above). 

`curl -X GET --header 'Content-Type: application/json' --header 'Accept: text/plain' -d 'https://api.openreq.eu/milla/requirementsInProject?projectId=QTWB' > QTWB-OpenReq.json`


A project can be sent to mulperi to be analyzed

`curl -X POST --header 'Content-Type: application/json' --header 'Accept: text/plain' -d 'QTWB' 'https://api.openreq.eu/milla/sendProjectToMulperi'` 

## Notes for Developers

None at the moment.

## Sources

None

# How to Contribute
See the OpenReq Contribution Guidelines [here](https://github.com/OpenReqEU/OpenReq/blob/master/CONTRIBUTING.md).

# License

Free use of this software is granted under the terms of the [EPL version 2 (EPL2.0)](https://www.eclipse.org/legal/epl-2.0/).

