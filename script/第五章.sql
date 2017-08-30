create table people (
  last_name  varchar(50) not null,
  first_name varchar(50) not null,
  dob        date not null,
  gender     enum('m', 'f') not null,
  key(last_name, first_name, dob)
);
insert into people values ('Bob', 'jos', '1989-09-11' , 'm');
