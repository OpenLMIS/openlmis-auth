--
-- Override default clients' initial resources to be none
--

UPDATE auth.oauth_client_details SET resourceids='' WHERE clientid IN ('trusted-client', 'user-client')
