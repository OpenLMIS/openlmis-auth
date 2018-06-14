CREATE TABLE email_verification_tokens (
    id uuid NOT NULL,
    expirydate timestamp with time zone NOT NULL,
    emailAddress CHARACTER VARYING(255) NOT NULL,
    userid uuid NOT NULL
);

ALTER TABLE ONLY email_verification_tokens
    ADD CONSTRAINT email_verification_tokens_pkey PRIMARY KEY (id),
    ADD CONSTRAINT email_verification_tokens_userid_uk UNIQUE (userid),
    ADD CONSTRAINT email_verification_tokens_emailaddress_uk UNIQUE (emailAddress),
    ADD CONSTRAINT email_verification_tokens_userid_fk FOREIGN KEY (userid) REFERENCES auth_users(id);
