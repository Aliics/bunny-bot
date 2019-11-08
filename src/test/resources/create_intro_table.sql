CREATE TABLE intro
(
    id         SERIAL PRIMARY KEY,
    discord_id BIGINT      NOT NULL,
    name       VARCHAR(50) NOT NULL,
    age        VARCHAR(12) NOT NULL,
    pronouns   VARCHAR(20),
    extra      TEXT
);
