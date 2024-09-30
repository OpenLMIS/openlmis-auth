CREATE TABLE password_reset_registries (
  id UUID PRIMARY KEY,
  userId UUID NOT NULL UNIQUE,
  lastAttemptDate TIMESTAMP WITH TIME ZONE NOT NULL,
  lastCounterResetDate TIMESTAMP WITH TIME ZONE NOT NULL,
  attemptCounter INTEGER,
  blocked BOOLEAN DEFAULT FALSE,

  CONSTRAINT fk_user FOREIGN KEY (userId) REFERENCES auth_users (id) ON DELETE CASCADE
);