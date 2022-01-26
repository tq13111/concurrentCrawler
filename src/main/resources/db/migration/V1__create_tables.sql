create table NEWS
(
    id          bigint primary key auto_increment,
    title       text,
    content     text,
    url         varchar(800),
    created_at  timestamp default now(),
    modified_at timestamp default now()
);

create table LINK_TO_BE_PROCESSED
(
    link varchar(800)
);

create table LINK_ALREADY_PROCESSED
(
    link varchar(800)
)