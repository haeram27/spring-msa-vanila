DROP TABLE IF EXISTS member;
CREATE TABLE member (
        id bigint generated by default as identity,
        name varchar(30) not null,
        age integer not null,
        primary key (id)
    );