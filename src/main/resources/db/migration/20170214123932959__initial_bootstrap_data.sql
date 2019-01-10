-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way. 
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

--
-- Data for Name: oauth_client_details; Type: TABLE DATA; Schema: auth; Owner: postgres
--

INSERT INTO auth.oauth_client_details (clientid, clientsecret, authorities, authorizedgranttypes,  resourceids, scope) VALUES ('trusted-client', 'secret', 'TRUSTED_CLIENT', 'client_credentials', 'auth,example,requisition,notification,referencedata,fulfillment,stockmanagement', 'read,write');
INSERT INTO auth.oauth_client_details (clientid, clientsecret, authorities, authorizedgranttypes,  resourceids, scope) VALUES ('user-client', 'changeme', 'TRUSTED_CLIENT', 'password', 'auth,example,requisition,notification,referencedata,fulfillment,stockmanagement', 'read,write');

--
-- Data for Name: auth_users; Type: TABLE DATA; Schema: auth; Owner: postgres
--

INSERT INTO auth.auth_users (id, enabled, password, role, username, email, referencedatauserid) VALUES ('51f6bdc1-4932-4bc3-9589-368646ef7ad3', 't', '$2a$10$4IZfidcJzbR5Krvj87ZJdOZvuQoD/kvPAJe549rUNoP3N3uH0Lq2G', 'ADMIN', 'admin', 'test@openlmis.org', '35316636-6264-6331-2d34-3933322d3462');
