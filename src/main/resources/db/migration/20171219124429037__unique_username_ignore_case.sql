-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way. 
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

CREATE FUNCTION remove_unique_username_index() RETURNS void AS $$
   DECLARE constraint_name VARCHAR(255);
   BEGIN
       SELECT
           tc.constraint_name
       INTO
           constraint_name
       FROM
           information_schema.table_constraints AS tc
           JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name
           JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name
       WHERE
           constraint_type = 'UNIQUE'
           AND tc.table_name='auth_users'
           AND kcu.column_name = 'username';

       EXECUTE 'ALTER TABLE auth_users DROP CONSTRAINT ' || constraint_name || ';';
   END;
$$ LANGUAGE plpgsql;

SELECT remove_unique_username_index();
DROP FUNCTION remove_unique_username_index();

DROP INDEX username_idx;
CREATE UNIQUE INDEX username_unq_idx on auth_users (lower(username));