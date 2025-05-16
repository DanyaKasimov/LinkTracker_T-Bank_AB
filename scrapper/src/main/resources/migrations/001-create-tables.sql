CREATE TABLE chats
(
    id      SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL
);

CREATE TABLE links
(
    id      SERIAL PRIMARY KEY,
    name    TEXT    NOT NULL,
    chat_id INTEGER NOT NULL REFERENCES chats (id)
);

CREATE TABLE filters
(
    id      SERIAL PRIMARY KEY,
    name    TEXT    NOT NULL,
    link_id INTEGER NOT NULL REFERENCES links (id)
);

CREATE TABLE tags
(
    id      SERIAL PRIMARY KEY,
    name    TEXT    NOT NULL,
    link_id INTEGER NOT NULL REFERENCES links (id)
);
