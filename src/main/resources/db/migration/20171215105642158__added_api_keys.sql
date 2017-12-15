CREATE TABLE api_keys (
    id uuid NOT NULL,
    clientid character varying(256) NOT NULL,
    serviceaccountid  uuid NOT NULL
);

ALTER TABLE ONLY api_keys
    ADD CONSTRAINT api_keys_pkey PRIMARY KEY (id);

ALTER TABLE api_keys
    ADD CONSTRAINT api_keys_clientid_serviceAccountId_uq UNIQUE (clientid, serviceAccountId);
