create database group_test_1;
create database group_test_2;
create database group_test_3;

use group_test_1;
drop table if exists crud;

CREATE TABLE crud (
  id int auto_increment,
  f1 int(10),
  f2 varchar(20),
  primary key(id)
);

#insert into crud(f1, f2) values(10, 'a1');
#insert into crud(f1, f2) values(11, 'b1');
#insert into crud(f1, f2) values(12, 'c1');
#insert into crud(f1, f2) values(13, 'd1');
#insert into crud(f1, f2) values(14, 'e1');
commit;

use group_test_2;
drop table if exists crud;

CREATE TABLE crud (
  id int auto_increment,
  f1 int(10),
  f2 varchar(20),
  primary key(id)
);

#insert into crud(f1, f2) values(10, 'a2');
#insert into crud(f1, f2) values(11, 'b2');
#insert into crud(f1, f2) values(12, 'c2');
#insert into crud(f1, f2) values(13, 'd2');
#insert into crud(f1, f2) values(14, 'e2');
commit;

use group_test_3;
drop table if exists crud;

CREATE TABLE crud (
  id int auto_increment,
  f1 int(10),
  f2 varchar(20),
  primary key(id)
);

#insert into crud(f1, f2) values(10, 'a3');
#insert into crud(f1, f2) values(11, 'b3');
#insert into crud(f1, f2) values(12, 'c3');
#insert into crud(f1, f2) values(13, 'd3');
#insert into crud(f1, f2) values(14, 'e3');
commit;
