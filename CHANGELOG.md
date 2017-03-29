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
