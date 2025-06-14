CREATE TABLE IF NOT EXISTS user_role
(
    id      BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT,
    role_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS unique_user_id_and_role_id ON user_role (user_id, role_id);