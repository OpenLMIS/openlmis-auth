ALTER TABLE email_verification_tokens
ADD COLUMN emailAddress CHARACTER VARYING(255) NOT NULL;
