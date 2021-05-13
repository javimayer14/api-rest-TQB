

insert into user(username,password,enabled) values ('Bruno','$2a$10$5pAOyS7Kdl0Bam4ZmjD0VOVBO6qx804V0pfTHSfxdKIw5DozkiX1m',1);
insert into user(username,password,enabled) values ('Mirta','$2a$10$Dbu3h9mNP4XkIG6Km3hjA.zdhGKuBCxBWBdTlNGdtAqkgMQp6H676',1);
insert into user(username,password,enabled) values ('Ricardo','$2a$10$Dbu3h9mNP4XkIG6Km3hjA.zdhGKuBCxBWBdTlNGdtAqkgMQp6H676',1);

insert into role (nombre) values ('ROLE_USER');
insert into role (nombre) values ('ROLE_ADMIN');

insert into user_role (usuario_id, role_id) values (1,1);
insert into user_role (usuario_id, role_id) values (1,2);
insert into user_role (usuario_id, role_id) values (2,2);
insert into user_role (usuario_id, role_id) values (2,1);
