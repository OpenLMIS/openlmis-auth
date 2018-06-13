UPDATE auth_users SET id = referencedatauserid;

ALTER TABLE auth_users DROP COLUMN referencedatauserid;
