--
-- Name: auth_users; Type: TABLE; Schema: auth; Owner: postgres; Tablespace:
--

CREATE TABLE auth_users (
    id uuid NOT NULL,
    email character varying(255) NOT NULL,
    enabled boolean,
    password character varying(255),
    referencedatauserid uuid NOT NULL,
    role character varying(255) NOT NULL,
    username character varying(255) NOT NULL
);


--
-- Name: oauth_access_token; Type: TABLE; Schema: auth; Owner: postgres; Tablespace:
--

CREATE TABLE oauth_access_token (
    tokenid character varying(256),
    token bytea,
    authenticationid character varying(256),
    username character varying(256),
    clientid character varying(256),
    authentication bytea,
    refreshtoken character varying(256)
);


--
-- Name: oauth_approvals; Type: TABLE; Schema: auth; Owner: postgres; Tablespace:
--

CREATE TABLE oauth_approvals (
    userid character varying(256),
    clientid character varying(256),
    scope character varying(256),
    status character varying(10),
    expiresat timestamp without time zone,
    lastmodifiedat timestamp without time zone
);


--
-- Name: oauth_client_details; Type: TABLE; Schema: auth; Owner: postgres; Tablespace:
--

CREATE TABLE oauth_client_details (
    clientid character varying(255) NOT NULL,
    accesstokenvalidity integer,
    additionalinformation character varying(255),
    authorities character varying(255),
    authorizedgranttypes character varying(255),
    autoapprove character varying(255),
    clientsecret character varying(255),
    refreshtokenvalidity integer,
    redirecturi character varying(255),
    resourceids character varying(255),
    scope character varying(255),
    webserverredirecturi character varying(255)
);


--
-- Name: oauth_client_token; Type: TABLE; Schema: auth; Owner: postgres; Tablespace:
--

CREATE TABLE oauth_client_token (
    tokenid character varying(256),
    token bytea,
    authenticationid character varying(256),
    username character varying(256),
    clientid character varying(256)
);


--
-- Name: oauth_code; Type: TABLE; Schema: auth; Owner: postgres; Tablespace:
--

CREATE TABLE oauth_code (
    code character varying(256),
    authentication bytea
);


--
-- Name: oauth_refresh_token; Type: TABLE; Schema: auth; Owner: postgres; Tablespace:
--

CREATE TABLE oauth_refresh_token (
    tokenid character varying(256),
    token bytea,
    authentication bytea
);


--
-- Name: password_reset_tokens; Type: TABLE; Schema: auth; Owner: postgres; Tablespace:
--

CREATE TABLE password_reset_tokens (
    id uuid NOT NULL,
    expirydate timestamp with time zone NOT NULL,
    userid uuid NOT NULL
);




--
-- Name: auth_users_pkey; Type: CONSTRAINT; Schema: auth; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY auth_users
    ADD CONSTRAINT auth_users_pkey PRIMARY KEY (id);


--
-- Name: oauth_client_details_pkey; Type: CONSTRAINT; Schema: auth; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY oauth_client_details
    ADD CONSTRAINT oauth_client_details_pkey PRIMARY KEY (clientid);


--
-- Name: password_reset_tokens_pkey; Type: CONSTRAINT; Schema: auth; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);


--
-- Name: uk_6jqfsuvys3lan090p4mk16a5t; Type: CONSTRAINT; Schema: auth; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY auth_users
    ADD CONSTRAINT uk_6jqfsuvys3lan090p4mk16a5t UNIQUE (email);


--
-- Name: uk_bghjccoicx7tll3ky3rf30sij; Type: CONSTRAINT; Schema: auth; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY auth_users
    ADD CONSTRAINT uk_bghjccoicx7tll3ky3rf30sij UNIQUE (referencedatauserid);


--
-- Name: uk_f9wqm8ya8k2x456jqotu3ihla; Type: CONSTRAINT; Schema: auth; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY auth_users
    ADD CONSTRAINT uk_f9wqm8ya8k2x456jqotu3ihla UNIQUE (username);


--
-- Name: uk_la2ts67g4oh2sreayswhox1i6; Type: CONSTRAINT; Schema: auth; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY password_reset_tokens
    ADD CONSTRAINT uk_la2ts67g4oh2sreayswhox1i6 UNIQUE (userid);


--
-- Name: fk_la2ts67g4oh2sreayswhox1i6; Type: FK CONSTRAINT; Schema: auth; Owner: postgres
--

ALTER TABLE ONLY password_reset_tokens
    ADD CONSTRAINT fk_la2ts67g4oh2sreayswhox1i6 FOREIGN KEY (userid) REFERENCES auth_users(id);

