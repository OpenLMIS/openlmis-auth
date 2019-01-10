CREATE TABLE api_keys (
    token UUID NOT NULL,
    createdBy uuid NOT NULL,
    createdDate timestamp with time zone NOT NULL
);

ALTER TABLE ONLY api_keys
    ADD CONSTRAINT api_keys_pkey PRIMARY KEY (token);
