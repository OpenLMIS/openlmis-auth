-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way. 
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

--
-- Override default clients' initial resources to be none
--

UPDATE auth.oauth_client_details SET resourceids='' WHERE clientid IN ('trusted-client', 'user-client')
