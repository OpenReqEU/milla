# Milla

This service was created as a result of the OpenReq project funded by the European Union Horizon 2020 Research and Innovation programme under grant agreement No 732463.


## Technical Description

Milla is an orchestrator (broker) service used in the Qt Jira trial of OpenReq. For further details, see the [swagger documentation](http://217.172.12.199:9203/swagger-ui.html).

## The following technologies are used:
	Spring Boot
	Maven 
	
## Public APIs

The API is documented by using Swagger2: http://217.172.12.199:9203/swagger-ui.html

## How to Install

Run the compiled jar file, e.g., nohup java -jar Milla-1.12.jar.

Milla uses port 9203 that needs to be open to in order that the Swagger page can be accessed. Milla also connects to Qt Jira ja the Mallikas and Mulperi services of OpenReq.

Milla requires at least Mallikas service to be running.

## How to Contribute
See the OpenReq Contribution Guidelines [here](https://github.com/OpenReqEU/OpenReq/blob/master/CONTRIBUTING.md).

## License

Free use of this software is granted under the terms of the [EPL version 2 (EPL2.0)](https://www.eclipse.org/legal/epl-2.0/).
