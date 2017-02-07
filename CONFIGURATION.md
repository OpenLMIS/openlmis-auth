# Configuration Guide

The following are a list of things that will need to be configured in order to properly use the 
Auth Service.

* User-based client ID and secret (default ID: user-client, secret: changeme) - Clients (like the 
Reference UI app) will use these credentials to log in and generate an access token on behalf of a 
user. Other browser apps, web apps or device apps that may allow users to log in to OpenLMIS would 
also use these User-based client credentials. For security purposes, these must be changed from the 
default. This can be configured by changing the row in the table `oauth_client_credentials`. 
* Service-based client ID and secret (default ID: trusted-client, secret: secret) - Only Services 
should use these credentials; for a Spring Boot application Service, these credentials are stored 
in the application.properties file. For security purposes, these must be changed from the default. 
This can be configured by changing the row in the table `oauth_client_credentials`.
* Duration of access token validity (in seconds, default 1800) - You can configure this by setting 
TOKEN_DURATION in your .env file for application startup.
