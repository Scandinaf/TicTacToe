CREATE TABLE GameInfo (
    id SERIAL PRIMARY KEY,
    game_id varchar(36) NOT NULL,
    incoming_message json NOT NULL,
    outgoing_message json NOT NULL
);