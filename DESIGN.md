# What is the Auth Service?

The Auth Service in OpenLMIS v3 is a stand-alone micro-service that implements OAuth 2. This is used by other OpenLMIS micro-services as well as by the Reference UI AngularJS application. It is also available for use by other mobile apps or web apps that may integrate with OpenLMIS.

The Auth Service maintains the list of user logins and passwords and it has endpoints for creating and verifying Tokens. It works with the **Reference Data service**, another micro-service that is required for an OpenLMIS implementation. The Reference Data service maintains its own list of users with additional properties as well as a list of **Rights** and **Roles** that may be associated with each user.

The Auth Service provides API [endpoints](http://openlmis.readthedocs.io/en/latest/api/index.html#auth-service) to generate or check a token as well as to handle user password resets (a forgot password workflow). The Reference Data service provides [endpoints](http://openlmis.readthedocs.io/en/latest/api/index.html#reference-data-service) for other services to check rights and manage user accounts, rights and role assignments. Both Auth and Reference Data work together in an OpenLMIS implementation to handle authentication and authorization in a way that can be leveraged and extended by other new services or third-party integrations.

## How Users Log In

In the reference distribution of OpenLMIS v3, the UI is an AngularJS browser application which connects to multiple APIs for the OpenLMIS services. These services are separate Spring Boot applications distributed as Docker images. Auth and Reference Data are two of these services, and they handle OpenLMIS user authentication and user authorization, respectively.

All of the services, including Auth, Reference Data, and the UI application, are available through one web URL. This is powered by NGINX and Consul. The benefit is that the UI and all the micro-services are available at one URL. For more about the micro-services architecture introduced with OpenLMIS 3, see the [Re-Architecture area of the wiki](https://openlmis.atlassian.net/wiki/display/OP/Re-Architecture).

When a user logs in and uses OpenLMIS, here is the typical sequence:

1. User opens a browser to OpenLMIS.
2. Browser loads the UI AngularJS application. The UI app starts running and presents a login screen.
3. User enters their username and password to log in.
4. UI app sends this username and password to the Auth Service API. (In OAuth2 terminology, the UI app is acting as a **Client** to the OAuth2 API in the Auth Service.)
5. If username and password are valid, the Auth Service responds to the UI app with a **Token**.
6. UI app receives this Token and uses it on each subsequent API request to any of the OpenLMIS micro-service APIs.
7. UI app loads the home screen of OpenLMIS and, in order to show the correct navigation options, it must query the Reference Data service for the user's rights.
8. UI app makes a request to the Reference Data service to retrieve the user account information and rights.
  a. This request includes the user's Token. The Token is a UUID string that is added to requests as a parameter, eg `/api/endpoint?access_token=57ec15d3-6a33-4165-96ad-94f2552fb5ba`.
9. Reference Data service responds to the UI app with the user account info including their **Roles and Rights**.
  a. Before responding, the Reference Data service first checks that the Token is valid, as follows:
  b. Reference Data makes a request to the Auth Service `/api/check_token` endpoint to test whether it is valid.
  c. If the Token is valid, Auth Service responds with user information, including the Reference Data UUID of the user account.
  d. Reference Data looks up its user account using the UUID.
  e. Reference Data looks up the Role and Rights of the user.
  f. Reference Data responds to the UI app with full object about the user account, including their Role and Rights.
10. UI app receives the user rights and uses that to provide their allowed navigation links.
11. User may navigate to any authorized areas of the OpenLMIS UI app.
  a. Different parts of the UI app are powered by different OpenLMIS micro-services such as Requisition or Fulfillment. Each time the UI app hits these different service APIs, every request includes the Token. Each service should check the token with a request to the Auth Service similar to two steps above. Furthermore, each service should check what Rights the user has in order to decide whether any particular request is allowed or not. For example, the Requisition Service uses Rights to determine whether the User has permissions to View or Approve any given Requisition.
12. Eventually, the User may log out of the Token may expire. Requests using that token would be
 rejected, and the UI app would invite the User to log in again.

For more about Roles and Rights, see the [Role-Based Access Control (RBAC) area of the wiki](https://openlmis.atlassian.net/wiki/display/OP/Role+Based+Access+Control).

## Auth Service Design

The Auth Service builds on the OAuth 2 protocol to implement a distributed, token-based authentication system. For background about OAuth 2 terminology, see [An Introduction to OAuth 2](https://www.digitalocean.com/community/tutorials/an-introduction-to-oauth-2). OpenLMIS builds onto the Java Spring framework OAuth 2 support and provides its own fields and endpoints beyond the core OAuth 2 specification.

The function of the Auth Service is to:

1. Generate access tokens for other Services to use.
2. Check access tokens for other Services and return the basic access level of the token provided.

Two endpoints in the Auth Service implement OAuth 2:

* `/api/oauth/token` - endpoint to generate an access token
* `/api/oauth/check_token` - endpoint to check an access token (Note: this endpoint is not included in the [Auth Service API documentation](http://openlmis.readthedocs.io/en/latest/api/index.html#auth-service).)

There are two types of access tokens that can be generated by the Auth Service:

1. **User-based tokens** - token is associated with a User, meaning a user account in the Reference Data service. The check_token endpoint returns a result with user info (username and Reference Data user UUID) with a USER or ADMIN authority. The USER or ADMIN authority currently has no meaning in the OpenLMIS system for access control. Access control is handled in role-based access control with Rights checks in the Reference Data Service. See the previous section for more info.
2. **Service-based tokens** - token is not associated with any User, and is strictly for Service-to-Service communication. The check_token endpoint returns a trusted client result with a TRUSTED_CLIENT authority. Only Services should use these tokens because they essentially give "root" level access in the system. All endpoints in OpenLMIS services are accessible if they are accessed with a valid Service-Based Token. For more information about Service-based tokens, see the section below.

### OAuth Client Credentials

Any service or app that accesses the Auth Service OAuth 2 endpoints is a **Client** in the OAuth 2 terminology. The OpenLMIS services and UI app each use credentials to connect to the Auth Service OAuth 2 endpoints. Each type of access token, User-based and Service-based, uses separate Client ID and Client credentials.

The Auth Service has a default client ID and secret for these two Client IDs:

* User-based Client ID: user-client, secret: changeme. The UI app uses these credentials to log in and generate an access token on behalf of a user. Other browser apps, web apps or device apps that may allow users to log in to OpenLMIS would also use these User-Based Client credentials.
* Service-based Client ID: trusted-client, secret: secret. Only Services should use these credentials; for a Spring Boot application Service, these credentials are stored in the application.properties file.

**Security Note:** For security purposes, implementers **must** change the ID and secret of both clients when deploying an OpenLMIS implementation. These credentials must be changed in the Auth service, in every other OpenLMIS service, and in the UI app.

In the Auth Service, the Client credentials are generated from the [Auth Service Bootstrap data](https://github.com/OpenLMIS/openlmis-auth/blob/master/src/main/resources/bootstrap.sql). The Auth Service also comes with [Demo Data](https://github.com/OpenLMIS/openlmis-auth/tree/master/demo-data) that includes demo user accounts. In the other OpenLMIS services, the credentials are stored in the Spring Boot application inside the application.properties file. EG, see the [Reference Data application.properties](https://github.com/OpenLMIS/openlmis-referencedata/blob/master/src/main/resources/application.properties) file. In the UI app, the Client credentials are stored in the [auth_server_client.json](https://github.com/OpenLMIS/openlmis-requisition-refUI/blob/master/src/main/resources/auth_server_client.json) file.

In short, implementors **must** change these credentials in all services. Bootstrap and Demo data are public knowledge and should not be used in any production system.

Inside the Auth Service, client credentials are stored in the database in the `oauth_client_details` table. Additionally, since user-based access tokens are associated with a User, basic User information is stored in the `auth_users` table. It includes username, password, email, and the Reference Data UUID of each User.

## Connecting a New Service to the Auth Service

If you are developing an additional service, you will want to connect to the Auth Service in order to use OpenLMIS authentication. The instructions below explain these steps for a Java Spring Boot application based off of the [OpenLMIS Template Service](https://github.com/OpenLMIS/openlmis-template-service).

To use the Auth Service to secure Service endpoints, follow these steps:

### Require a Token from the Auth Service

Your service endpoints should check that users are properly authenticated by requiring an access token from the Auth Service. This is done by adding Configuration classes in Java for a Resource Server.

The [Requisition Service](https://github.com/OpenLMIS/openlmis-requisition) gives a good blueprint for implementing this. See [ResourceServerSecurityConfiguration.java and MethodSecurityConfiguration.java](https://github.com/OpenLMIS/openlmis-requisition/tree/master/src/main/java/org/openlmis/security).

The Configuration classes will need to have values for the Auth Service check_token URL, as well as the client ID and client secret, which are set in the application.properties file of the Spring Boot application.

Which endpoints are secured or unsecured are specified in the configure(HttpSecurity) method in the ResourceServerSecurityConfiguration class. This is done by specifying things like permitAll() or fullyAuthenticated().

### Check Rights using the Reference Data Service

Individual endpoints need to have an authorization check to ensure the user accessing the endpoint has the correct right(s) according to Role-Based Access Control (RBAC). RBAC is provided by the OpenLMIS Reference Data service.

For most Services, including any new Services, this authorization check is done by making a call to the Reference Data Service's hasRight endpoint (`/api/users/<userId>/hasRight`). See the [Reference Data API docs](http://openlmis.readthedocs.io/en/latest/api/index.html#reference-data-service) for details.

Depending on the Right type, some additional parameters may need to be specified, in addition to the right ID.

* For supervision rights, the facility ID and program ID are required
* For order fulfillment rights, the warehouse ID is required
* For admin rights, nothing else is required

See the PermissionService.hasPermission() methods in the Requisition Service for an example.

Note: In the Java code inside the Reference Data Service, since the RBAC code checking is internal, a direct call to the User.hasRight() method is often made. See the RightService.checkAdminRight() methods in the Reference Data Service for an example. But any other services or a new service you are developing need to use an HTTP API request to the endpoint to check rights.

## Adding Service-Level Rights to a Service

### When is this used?

Service-Level rights should be used sparingly where user-based rights are not sufficient. In certain cases, one Service may access another's endpoints using Service-Level rights that allow "root" access. For example, an endpoint to get facility details normally requires a "manage facilities" right, but the Requisition Service needs to access facility details to perform requisition workflow on behalf of a user who does not have the "manage facilities" right. This is one of the scenarios where service-level rights are necessary.

Service-Level rights use service-based access tokens to give "root" access to an endpoint. The normal role-based access (RBAC) for a user is not checked, and any request is allowed with a valid service-based access token.

### How can my service use service-level rights to make requests from other services?

If you have an endpoint in a Java Sprint Boot application that needs to use a service-based access token, here are the basic steps:

* The endpoint obtains a service-level access token (see [BaseCommunicationService](https://github.com/OpenLMIS/openlmis-requisition/blob/master/src/main/java/org/openlmis/requisition/service/BaseCommunicationService.java) in the Requisition Service). Note: Credentials for this token will come from the application.properties file; see the Security warning above. 
* Make an HTTP request to another service URI using the service-level access token (see [BaseReferenceDataService](https://github.com/OpenLMIS/openlmis-requisition/blob/master/src/main/java/org/openlmis/requisition/service/referencedata/BaseReferenceDataService.java) in the Requisition Service).
* Use caution when calling endpoints that may change data, and use caution with the data returned. In most cases, you might use that data in some business logic, but you should not return that data to your end-user, because they might not have access to view all that data (such as all the Facility data in the requisition example above).

### How can my service accept service-level rights?

If you have an endpoint in a Java Spring Boot application that needs to accept requests from other services with service-level rights, here are the basic steps:

* The endpoint can get the Authentication object from the application's security context.
* Check that the authentication isClientOnly() (meaning it does not have an OpenLMIS User behind it; true for service-based access tokens), if true, it can allow the request to continue.
* Otherwise, it would stop and return an Unauthorized HTTP code.

See the RightService.checkAdminRight() methods in the Reference Data Service for an example.