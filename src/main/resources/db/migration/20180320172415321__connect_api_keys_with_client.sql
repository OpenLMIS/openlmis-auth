ALTER TABLE auth.api_keys
ADD COLUMN clientid character varying(255) NOT NULL;

ALTER TABLE ONLY auth.api_keys
ADD CONSTRAINT api_keys_clientid_fk FOREIGN KEY (clientid) REFERENCES auth.oauth_client_details (clientid);
