3.2.0 / WIP
===================

Improvements and new functionality which are backwards-compatible:
* [OLMIS-3135](https://openlmis.atlassian.net/browse/OLMIS-3135): Generating access tokens for API Keys 

Bug fixes that are backwards-compatible:
* [OLMIS-3537](https://openlmis.atlassian.net/browse/OLMIS-3537): Usernames are now case-insensitive. (Note: Implementations need to handle this before upgrading this component, if they use usernames that differ by letter casing only)
* [OLMIS-3778](https://openlmis.atlassian.net/browse/OLMIS-3778): Fixed service checks the rights of a wrong user
* [OLMIS-4176](https://openlmis.atlassian.net/browse/OLMIS-4176): Removed string response message from reset password response.

3.1.1 / 2017-11-09
===================

Bug fixes added in a backwards-compatible manner:
* [OLMIS-3119](https://openlmis.atlassian.net/browse/OLMIS-3119): Fixed issue with TOKEN_DURATION variable being ingored, which
in reality was an issue with set up of the Spring context and autowiring not working as expected.
* [OLMIS-3357](https://openlmis.atlassian.net/browse/OLMIS-3357): Reset email will not be sent when user is created or updated

3.1.0 / 2017-09-01
===================

Improvements and new functionality which are backwards-compatible:
* [OLMIS-1498](https://openlmis.atlassian.net/browse/OLMIS-1498): The service will now fetch list of available services from consul, and update OAuth2 resources dynamically when a new service is registered or de-registered. Those tokens are no longer hard-coded.
* [OLMIS-2812](https://openlmis.atlassian.net/browse/OLMIS-2812): Index username column
  * This column appears to be used frequently by the user details implemtation, indexing should help performance.
* [OLMIS-2851](https://openlmis.atlassian.net/browse/OLMIS-2851): Let external applications (that run in a browser) access our APIs
  * Allow for implicit grant flow.
  * Expose every endpoint that oauth2 has and document them with RAML.
  * Add CORS support.
  * Add tableau-wdc client to the demo data.
* [OLMIS-2866](https://openlmis.atlassian.net/browse/OLMIS-2866): The service will no longer used self-contained user roles (USER, ADMIN), and depend solely on referencedata's roles for user management.
* [OLMIS-2871](https://openlmis.atlassian.net/browse/OLMIS-2871): The service now uses an Authorization header instead of an access_token request parameter when communicating with other services.
* [OLMIS-2957](https://openlmis.atlassian.net/browse/OLMIS-2957): Enable tomcat access logs
* [OLMIS-2619](https://openlmis.atlassian.net/browse/OLMIS-2619): Add cce manager user and remove redundant fields from auth demo data

3.0.3 / 2017-06-23
===================

Improvements which are backwards-compatible:
* [OLMIS-2611](https://openlmis.atlassian.net/browse/OLMIS-2611): Added using locale from env file.

3.0.2 / 2017-05-08
===================

Improvements which are backwards-compatible:
* [OLMIS-2155](https://openlmis.atlassian.net/browse/OLMIS-2155): Migrated service to Spring Boot 1.4.1
  * Performance issue with custom ZonedDateTimeAttributeConverter.
* [OLMIS-2267](https://openlmis.atlassian.net/browse/OLMIS-2267): Remove email from auth-user, pull email from referenceda service
  * This change was part of making email optional for user setup.

Dev and tooling updates made in a backwards-compatible manner:
* [OLMIS-1972](https://openlmis.atlassian.net/browse/OLMIS-1972): Update Postgres from 9.4 to 9.6
  * This upgrade will apply automatically and all data will migrate.
* Update [Docker Dev Image](https://github.com/OpenLMIS/docker-dev) for builds from v1 to v2
  * Moves the sync_transifex.sh script out of each service and into the Docker Dev Image.

3.0.1 / 2017-03-29
==================

New functionality added in a backwards-compatible manner:

* [OLMIS-1694](https://openlmis.atlassian.net/browse/OLMIS-1694): Remove ReferenceData to Auth dependency
  * Generate password reset token and notify the user after creating an account (moved from
  Reference Data).
  * Check USERS_MANAGE right when saving users.

3.0.0 / 2017-03-01
==================

* Released openlmis-auth 3.0.0 as part of openlmis-ref-distro 3.0.0. See [3.0.0 Release Notes](https://openlmis.atlassian.net/wiki/display/OP/3.0.0+Release+Notes).
 * This was the first stable release of openlmis-auth. It builds on the code, patterns, and lessons
 learned from OpenLMIS 1 and 2.
