create table if not exists person
(
    id          int primary key auto_increment,
    name        varchar(20),
    `age` int,
    imgs  binary,
    create_time datetime default current_timestamp(),
    update_time datetime default current_timestamp() on update current_timestamp()
);
truncate table person;

insert into person(id, name, `age`)
values (1, 'aba', 1),
       (2, 'abb', 2),
       (3, 'abc', null)
;