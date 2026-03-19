insert into gestao.empresa (criado_em, modificado_em, nome_fantasia, razao_social, slug, status, plano, ativo)
values (now(), now(), 'Loja Padrao', 'Loja Padrao', 'loja-padrao', 'ATIVA', 'BASICO', true);

insert into gestao.perfil (criado_em, criado_por, modificado_em, modificado_por, nome)
values (now(), 1, now(), 1, 'ADMIN_EMPRESA');

insert into gestao.perfil (criado_em, criado_por, modificado_em, modificado_por, nome)
values (now(), 1, now(), 1, 'GERENTE');

insert into gestao.perfil (criado_em, criado_por, modificado_em, modificado_por, nome)
values (now(), 1, now(), 1, 'OPERADOR');

-- Para o primeiro ADMIN_SAAS, prefira usar as variaveis:
-- APP_BOOTSTRAP_ADMIN_SAAS_NOME
-- APP_BOOTSTRAP_ADMIN_SAAS_USUARIO
-- APP_BOOTSTRAP_ADMIN_SAAS_SENHA
