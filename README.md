# OpenLMIS Authentication Service
This repository holds the files for OpenLMIS Authentication Independent Service.

## Prerequisites
* Docker 1.11+
* Docker Compose 1.6+

## Quick Start
1. Fork/clone this repository from GitHub.

 ```shell
 git clone https://github.com/OpenLMIS/openlmis-auth.git
 ```
2. Add an environment file called `.env` to the root folder of the project, with the required 
project settings and credentials. For a starter environment file, you can use [this 
one](https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env). e.g.

 ```shell
 cd openlmis-auth
 curl -o .env -L https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env
 ```
3. Develop w/ Docker by running `docker-compose run --service-ports auth`.
See [Developing w/ Docker](#devdocker).
4. You should now be in an interactive shell inside the newly created development 
environment, start the Service with: `gradle bootRun`
5. Go to `http://<yourDockerIPAddress>:8080/` to see the service name 
and version. Note that you can determine yourDockerIPAddress by running `docker-machine ip`.
6. Go to `http://<yourDockerIPAddress>:8080/api?access_token=<access_token_id>` to see the APIs.
For additional info about security see the Security section in the Example Service README at
https://github.com/OpenLMIS/openlmis-example/blob/master/README.md#security.

## Service Design
See the [Design document](DESIGN.md).

## API Definition and Testing
See the API Definition and Testing section in the Example Service README at
https://github.com/OpenLMIS/openlmis-example/blob/master/README.md#api.

## Building & Testing
See the Building & Testing section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#building.

## <a name="devdocker">Developing with Docker</a>
See the Developing with Docker section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#devdocker.

### Development Environment
See the Development Environment section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#devenv.

### Build Deployment Image
See the Build Deployment Image section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#buildimage.

### Publish to Docker Repository
TODO

### Docker's file details
See the Docker's file details section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#dockerfiles.

### Running complete application with nginx proxy
See the Running complete application with nginx proxy section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#nginx.

### Logging
See the Logging section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#logging.

### Internationalization (i18n)
See the Internationalization section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#internationalization.

### Debugging
See the Debugging section in the Service Template README at
https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#debugging.

## Production by Spring Profile

By default when this service is started, it will clean it's schema in the database before migrating
it.  This is meant for use during the normal development cycle.  For production data, this obviously
is not desired as it would remove all of the production data.  To change the default clean & migrate
behavior to just be a migrate behavior (which is still desired for production use), we use a Spring
Profile named `production`.  To use this profile, it must be marked as Active.  The easiest way to
do so is to add to the .env file:

```java
spring_profiles_active=production
```

This will set the similarly named environment variable and limit the profile in use.  The
expected use-case for this is when this service is deployed through the
[Reference Distribution](https://github.com/openlmis/openlmis-ref-distro).

### Demo Data
A basic set of demo data is included with this service, defined under `./demo-data/`.  This data may
be optionally loaded by using the `demo-data` Spring Profile.  Setting this profile may be done by
setting the `spring.profiles.active` environment variable.

When building locally from the development environment, you may run:

```shell
$ export spring_profiles_active=demo-data
$ gradle bootRun
```

To see how to set environment variables through Docker Compose, see the 
[Reference Distribution](https://github.org/openlmis/openlmis-ref-distro)


## Environment variables

Environment variables common to all services are listed here: https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#environment-variables

The auth service also uses the following variables:
* **TOKEN_DURATION** - The period of inactivity in seconds after which authentication tokens will expire. For example set this to 900 in order to have tokens expire after 15 minutes of inactivity. The default value is 1800 (30 minutes).
