CREATE TABLE IF NOT EXISTS t_book(
    id          bigint auto_increment,
    create_time timestamp,
    update_time timestamp,
    book_name   varchar(255),
    author      varchar(255),
    image       varchar(255),
    description varchar(255),
    isbn        varchar(255),
    rating      double,
    keywords    varchar(2047),
    primary key (id)
);