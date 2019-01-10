-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way. 
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

ALTER TABLE auth.api_keys
ADD COLUMN clientid character varying(255) NOT NULL;

ALTER TABLE ONLY auth.api_keys
ADD CONSTRAINT api_keys_clientid_fk FOREIGN KEY (clientid) REFERENCES auth.oauth_client_details (clientid);
