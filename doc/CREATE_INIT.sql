insert into gestao.perfil (criado_em,criado_por,modificado_em,modificado_por,nome) values (now(),1,now(),1,'ROLE_ADMIN');

insert into gestao.usuario (criado_em,modificado_em,nome_completo,usuario,senha) values (now(),now(),'Reginaldo Alves Domingos Junior','RADJUNIOR','$2a$12$6XC9yToEq7wWvrvsnryeLOvXMlRHoMVLoIeirBV5jc5hFXITcx80C');
insert into gestao.usuario_perfil (perfil_id,usuario_id) values (1,1);

insert into gestao.usuario (criado_em,modificado_em,nome_completo,usuario,senha) values (now(),now(),'Fabio Roberto Siqueira','FSIQUEIRA','$2a$12$gbshIq5XVZkLG31hhLpBqeBTbeBl/wjTxJjOu.tn/PkA9KjFZ/iOi');
insert into gestao.usuario_perfil (perfil_id,usuario_id) values (1,2);

update gestao.usuario set criado_por = 1, modificado_por = 1;

select * from gestao.usuario;
select * from gestao.perfil;
select * from gestao.usuario_perfil;