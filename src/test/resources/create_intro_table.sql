CREATE TABLE intro
(
    id         SERIAL PRIMARY KEY,
    discord_id VARCHAR(18) NOT NULL,
    name       VARCHAR(50) NOT NULL,
    age        VARCHAR(12) NOT NULL,
    pronouns   VARCHAR(20),
    extra      TEXT
);
