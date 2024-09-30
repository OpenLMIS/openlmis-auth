CREATE TABLE unsuccessful_authentication_attempts (
    id UUID PRIMARY KEY,
    userId UUID NOT NULL UNIQUE,
    lastUnsuccessfulAuthenticationAttemptDate TIMESTAMP WITH TIME ZONE NOT NULL,
    attemptCounter INTEGER,

    CONSTRAINT fk_user FOREIGN KEY (userId) REFERENCES auth_users (id) ON DELETE CASCADE
);
