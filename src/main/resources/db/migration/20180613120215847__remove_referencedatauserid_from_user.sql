ALTER TABLE ONLY password_reset_tokens
    DROP CONSTRAINT fk_la2ts67g4oh2sreayswhox1i6;

UPDATE password_reset_tokens AS t
    SET userid = u.referencedatauserid
    FROM auth_users AS u
    WHERE t.userid = u.id;

UPDATE auth_users
    SET id = referencedatauserid;

ALTER TABLE auth_users
    DROP COLUMN referencedatauserid;
ALTER TABLE ONLY password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_user_id_fk FOREIGN KEY (userid) REFERENCES auth_users(id);
