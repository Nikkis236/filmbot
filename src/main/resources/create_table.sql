create schema bot;

alter schema bot owner to postgres;

create table bot.USER_BOOKMARK
(
    ID serial,
    CHAT_ID varchar not null,
    MOVIE_ID varchar not null
);

create unique index user_bookmark_id_uindex
    on bot.USER_BOOKMARK (ID);

alter table bot.USER_BOOKMARK
    add constraint user_bookmark_pk
        primary key (ID);

