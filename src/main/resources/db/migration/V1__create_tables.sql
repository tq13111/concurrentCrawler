create table news
(
    id          bigint primary key auto_increment,
    title       text,
    content     text,
    url         varchar(800),
    created_at  timestamp,
    modified_at timestamp
);

create table LINK_TO_BE_PROCESSED
(
    link varchar(800),
);

create table LINK_ALREADY_PROCESSED
(
    link varchar(800),
)