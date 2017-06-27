3.0.4 / WIP
===================

Improvements which are backwards-compatible:
* [OLMIS-1498](https://openlmis.atlassian.net/browse/OLMIS-1498): The service will now fetch list of available services from consul,
and update OAuth2 resources dynamically when a new service is registered or de-registered. Those tokens are no longer hard-coded.

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
