
INSERT INTO oauth_client_details (client_id, client_secret, authorities, authorized_grant_types,  resource_ids, scope)
VALUES ('trusted-client', 'secret', 'TRUSTED_CLIENT', 'client_credentials,password', 'auth,example,requisition', 'read,write');

INSERT INTO auth_users (id, enabled, password, role, username, email, reference_data_user_id)
VALUES ('51f6bdc1-4932-4bc3-9589-368646ef7ad3', 't', '$2a$10$4IZfidcJzbR5Krvj87ZJdOZvuQoD/kvPAJe549rUNoP3N3uH0Lq2G', 'ADMIN', 'admin', 'test@openlmis.org', '51f6bdc1-4932-4bc3-9589-368646ef7ad3');
