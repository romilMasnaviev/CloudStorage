CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT PRIMARY KEY,
    username VARCHAR(50) unique,
    password VARCHAR(100)
);


CREATE TABLE IF NOT EXISTS roles
(
    id   BIGINT PRIMARY KEY,
    role varchar(50)
);


CREATE TABLE IF NOT EXISTS user_role
(
    id      BIGINT PRIMARY KEY,
    user_id BIGINT,
    role_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id)
);
ALTER TABLE user_role
    ADD CONSTRAINT unique_user_id_and_role_id UNIQUE (user_id, role_id);