# Plano de Testes Multiempresa

## 1. Preparacao

1. Aplicar o script [MIGRACAO_MULTIEMPRESA.sql](/C:/Workspace/sistema-gestao/doc/MIGRACAO_MULTIEMPRESA.sql) em homologacao.
2. Subir a aplicacao com um usuario `ADMIN_SAAS` inicial usando as variaveis:
   - `APP_BOOTSTRAP_ADMIN_SAAS_NOME`
   - `APP_BOOTSTRAP_ADMIN_SAAS_USUARIO`
   - `APP_BOOTSTRAP_ADMIN_SAAS_SENHA`
3. Confirmar que o login do `ADMIN_SAAS` funciona e que o menu `SaaS > Empresas` aparece.

## 2. Casos de teste funcionais

1. Criacao de empresa
   - Criar uma empresa com slug unico e administrador inicial.
   - Validar mensagem de sucesso.
   - Confirmar que a empresa aparece na lista.

2. Login do ADMIN_EMPRESA
   - Entrar com o login criado para a empresa.
   - Confirmar que o nome da empresa aparece no topo.
   - Confirmar que o menu `Cadastro` e `Usuarios` aparece.

3. Isolamento entre empresas
   - Criar Empresa A e Empresa B.
   - Logar na Empresa A e cadastrar marca, categoria, grupo, subgrupo e produto.
   - Logar na Empresa B e confirmar que nenhum desses dados aparece.

4. Combos filtrados por empresa
   - Na Empresa A, abrir cadastro de produto.
   - Confirmar que os combos de marca, categoria, grupo e subgrupo mostram apenas dados da Empresa A.
   - Repetir na Empresa B.

5. Edicao e exclusao no mesmo escopo
   - Editar e excluir registros criados pela empresa logada.
   - Confirmar que operacoes funcionam normalmente.

6. Bloqueio de acesso cruzado
   - Copiar a URL de edicao de um produto da Empresa A.
   - Logar com usuario da Empresa B e acessar a mesma URL.
   - Confirmar erro de acesso ou registro nao encontrado.

7. Gestao de usuarios da empresa
   - Logar como `ADMIN_EMPRESA`.
   - Criar usuario com perfil `OPERADOR`.
   - Criar usuario com perfil `GERENTE`.
   - Editar usuario sem trocar senha.
   - Editar usuario trocando senha.
   - Excluir usuario.

8. Restricao de perfil ADMIN_SAAS
   - Logar como `ADMIN_EMPRESA`.
   - Tentar atribuir `ADMIN_SAAS`.
   - Confirmar que o perfil nao aparece na tela e nao pode ser salvo.

9. Suspensao de empresa
   - Logar como `ADMIN_SAAS`.
   - Editar empresa e marcar status `SUSPENSA`.
   - Tentar login com usuario dessa empresa.
   - Confirmar bloqueio de acesso.

## 3. Casos de validacao

1. Empresa com slug duplicado deve falhar.
2. Empresa com CNPJ duplicado deve falhar.
3. Usuario com login duplicado deve falhar.
4. Produto com SKU duplicado dentro da mesma empresa deve falhar.
5. Produto com SKU igual em empresas diferentes deve funcionar.
6. Marca, categoria, grupo e subgrupo com nome duplicado dentro da mesma empresa devem falhar.
7. Marca, categoria, grupo e subgrupo com nome igual em empresas diferentes devem funcionar.
8. Subgrupo ligado a grupo de outra empresa deve falhar.
9. Produto ligado a marca/categoria/grupo/subgrupo de outra empresa deve falhar.

## 4. Casos de seguranca

1. `ADMIN_SAAS` deve acessar `/admin/empresas`.
2. `ADMIN_EMPRESA` nao deve acessar `/admin/empresas`.
3. `GERENTE` e `OPERADOR` nao devem acessar `/empresa/usuarios`.
4. Usuario sem empresa nao deve acessar `/cadastro/**`.

## 5. Verificacoes tecnicas

1. Executar `mvn -q -DskipTests compile`.
2. Executar `mvn test` apos configurar um banco homologado compatível com o schema novo.
3. Validar no banco:
   - `empresa_id` preenchido nas tabelas de negocio.
   - indices unicos por empresa criados.
   - usuarios vinculados corretamente as empresas.

## 6. Checklist de regressao

1. Login e logout continuam funcionando.
2. Home continua abrindo apos autenticacao.
3. CRUD de produto continua calculando preco e gerando SKU.
4. CRUD de marca, categoria, grupo e subgrupo continua funcionando.
5. Tela 403 continua abrindo quando houver acesso negado.
