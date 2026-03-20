---------------------------------------------------------------------------------------------------------------------------------------
-- Zerando a Base

delete from gestao.empresa;
delete from gestao.usuario;
delete from gestao.perfil;
delete from gestao.usuario_perfil;

---------------------------------------------------------------------------------------------------------------------------------------
-- Criando Usuários

insert into gestao.usuario (criado_em,modificado_em,nome_completo,usuario,senha,ativo) 
values (now(),now(),'Administrador','ADMIN','$2a$12$a0bk/DBsZjryqqjlus/gH.uxUnCgB12TFtRRw2eblNU7E.p75qsr6',true);

insert into gestao.usuario (criado_em,modificado_em,nome_completo,usuario,senha,ativo) 
values (now(),now(),'Reginaldo Junior','RADJUNIOR','$2a$12$6XC9yToEq7wWvrvsnryeLOvXMlRHoMVLoIeirBV5jc5hFXITcx80C',true);

insert into gestao.usuario (criado_em,modificado_em,nome_completo,usuario,senha,ativo) 
values (now(),now(),'Fabio Roberto Siqueira','FSIQUEIRA','$2a$12$gbshIq5XVZkLG31hhLpBqeBTbeBl/wjTxJjOu.tn/PkA9KjFZ/iOi',true);

update 	gestao.usuario 
set 	criado_por = (select id from gestao.usuario where usuario = 'ADMIN'), 
		modificado_por = (select id from gestao.usuario where usuario = 'ADMIN')
where 	id > 0;
---------------------------------------------------------------------------------------------------------------------------------------
-- Criando Empresa

insert into gestao.empresa (criado_em, modificado_em, nome_fantasia, razao_social, slug, status, plano, ativo)
values (now(), now(), 'Ester Siqueira Moda', 'ESTER SIQUEIRA MODA', 'ester-siquera-moda', 'ATIVA', 'BASICO', true);

update gestao.usuario 
set empresa_id = (select max(id) from gestao.empresa where slug = 'ester-siquera-moda') 
where usuario <> 'ADMIN';

---------------------------------------------------------------------------------------------------------------------------------------
-- Criando Perfis
insert into gestao.perfil (criado_em, modificado_em, nome) values (now(), now(), 'ADMIN_SAAS');
insert into gestao.perfil (criado_em, modificado_em, nome) values (now(), now(), 'ADMIN_EMPRESA');
insert into gestao.perfil (criado_em, modificado_em, nome) values (now(), now(), 'GERENTE');
insert into gestao.perfil (criado_em, modificado_em, nome) values (now(), now(), 'OPERADOR');

update 	gestao.perfil
set 	criado_por = (select id from gestao.usuario where usuario = 'ADMIN'),
		modificado_por = (select id from gestao.usuario where usuario = 'ADMIN')
where 	id > 0;

---------------------------------------------------------------------------------------------------------------------------------------
-- Associando Perfis a Usuários

insert into gestao.usuario_perfil (perfil_id,usuario_id) 
values ((select id from gestao.perfil where nome = 'ADMIN_SAAS'), (select id from gestao.usuario where usuario = 'ADMIN'));

insert into gestao.usuario_perfil (perfil_id,usuario_id) 
values ((select id from gestao.perfil where nome = 'ADMIN_EMPRESA'), (select id from gestao.usuario where usuario = 'RADJUNIOR'));

insert into gestao.usuario_perfil (perfil_id,usuario_id) 
values ((select id from gestao.perfil where nome = 'ADMIN_EMPRESA'), (select id from gestao.usuario where usuario = 'FSIQUEIRA'));

---------------------------------------------------------------------------------------------------------------------------------------
-- Testes
select * from gestao.empresa;
select * from gestao.usuario;
select * from gestao.perfil;
select * from gestao.usuario_perfil;