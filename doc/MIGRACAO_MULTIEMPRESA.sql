-- Migracao base para introduzir multiempresa no schema "gestao".
-- Revise em ambiente de homologacao antes de executar em producao.

BEGIN;

CREATE TABLE IF NOT EXISTS gestao.empresa (
    id BIGSERIAL PRIMARY KEY,
    criado_em TIMESTAMP,
    modificado_em TIMESTAMP,
    criado_por BIGINT,
    modificado_por BIGINT,
    nome_fantasia VARCHAR(150) NOT NULL,
    razao_social VARCHAR(150),
    cnpj VARCHAR(20),
    email VARCHAR(150),
    telefone VARCHAR(20),
    slug VARCHAR(80) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    plano VARCHAR(30) NOT NULL DEFAULT 'BASICO',
    data_inicio DATE,
    data_vencimento DATE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_empresa_slug ON gestao.empresa (LOWER(slug));
CREATE UNIQUE INDEX IF NOT EXISTS uk_empresa_cnpj ON gestao.empresa (cnpj) WHERE cnpj IS NOT NULL;

INSERT INTO gestao.empresa (nome_fantasia, razao_social, slug, status, plano, ativo, criado_em, modificado_em)
SELECT 'LOJA PADRAO', 'LOJA PADRAO', 'loja-padrao', 'ATIVA', 'BASICO', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM gestao.empresa WHERE LOWER(slug) = 'loja-padrao');

ALTER TABLE gestao.usuario ADD COLUMN IF NOT EXISTS empresa_id BIGINT;
ALTER TABLE gestao.usuario ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE gestao.produto ADD COLUMN IF NOT EXISTS empresa_id BIGINT;
ALTER TABLE gestao.marca ADD COLUMN IF NOT EXISTS empresa_id BIGINT;
ALTER TABLE gestao.categoria ADD COLUMN IF NOT EXISTS empresa_id BIGINT;
ALTER TABLE gestao.grupo ADD COLUMN IF NOT EXISTS empresa_id BIGINT;
ALTER TABLE gestao.subgrupo ADD COLUMN IF NOT EXISTS empresa_id BIGINT;

UPDATE gestao.usuario
SET empresa_id = (SELECT id FROM gestao.empresa WHERE LOWER(slug) = 'loja-padrao')
WHERE empresa_id IS NULL;

UPDATE gestao.produto
SET empresa_id = (SELECT id FROM gestao.empresa WHERE LOWER(slug) = 'loja-padrao')
WHERE empresa_id IS NULL;

UPDATE gestao.marca
SET empresa_id = (SELECT id FROM gestao.empresa WHERE LOWER(slug) = 'loja-padrao')
WHERE empresa_id IS NULL;

UPDATE gestao.categoria
SET empresa_id = (SELECT id FROM gestao.empresa WHERE LOWER(slug) = 'loja-padrao')
WHERE empresa_id IS NULL;

UPDATE gestao.grupo
SET empresa_id = (SELECT id FROM gestao.empresa WHERE LOWER(slug) = 'loja-padrao')
WHERE empresa_id IS NULL;

UPDATE gestao.subgrupo
SET empresa_id = (SELECT id FROM gestao.empresa WHERE LOWER(slug) = 'loja-padrao')
WHERE empresa_id IS NULL;

ALTER TABLE gestao.produto ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE gestao.marca ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE gestao.categoria ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE gestao.grupo ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE gestao.subgrupo ALTER COLUMN empresa_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_usuario_empresa') THEN
        ALTER TABLE gestao.usuario
            ADD CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id) REFERENCES gestao.empresa(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_produto_empresa') THEN
        ALTER TABLE gestao.produto
            ADD CONSTRAINT fk_produto_empresa FOREIGN KEY (empresa_id) REFERENCES gestao.empresa(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_marca_empresa') THEN
        ALTER TABLE gestao.marca
            ADD CONSTRAINT fk_marca_empresa FOREIGN KEY (empresa_id) REFERENCES gestao.empresa(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_categoria_empresa') THEN
        ALTER TABLE gestao.categoria
            ADD CONSTRAINT fk_categoria_empresa FOREIGN KEY (empresa_id) REFERENCES gestao.empresa(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_grupo_empresa') THEN
        ALTER TABLE gestao.grupo
            ADD CONSTRAINT fk_grupo_empresa FOREIGN KEY (empresa_id) REFERENCES gestao.empresa(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_subgrupo_empresa') THEN
        ALTER TABLE gestao.subgrupo
            ADD CONSTRAINT fk_subgrupo_empresa FOREIGN KEY (empresa_id) REFERENCES gestao.empresa(id);
    END IF;
END $$;

DO $$
DECLARE constraint_name TEXT;
BEGIN
    SELECT tc.constraint_name
      INTO constraint_name
      FROM information_schema.table_constraints tc
      JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name
     WHERE tc.table_schema = 'gestao'
       AND tc.table_name = 'produto'
       AND tc.constraint_type = 'UNIQUE'
       AND ccu.column_name = 'sku'
     LIMIT 1;

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE gestao.produto DROP CONSTRAINT %I', constraint_name);
    END IF;
END $$;

DO $$
DECLARE constraint_name TEXT;
BEGIN
    SELECT tc.constraint_name
      INTO constraint_name
      FROM information_schema.table_constraints tc
      JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name
     WHERE tc.table_schema = 'gestao'
       AND tc.table_name = 'marca'
       AND tc.constraint_type = 'UNIQUE'
       AND ccu.column_name = 'nome'
     LIMIT 1;

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE gestao.marca DROP CONSTRAINT %I', constraint_name);
    END IF;
END $$;

DO $$
DECLARE constraint_name TEXT;
BEGIN
    SELECT tc.constraint_name
      INTO constraint_name
      FROM information_schema.table_constraints tc
      JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name
     WHERE tc.table_schema = 'gestao'
       AND tc.table_name = 'categoria'
       AND tc.constraint_type = 'UNIQUE'
       AND ccu.column_name = 'nome'
     LIMIT 1;

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE gestao.categoria DROP CONSTRAINT %I', constraint_name);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_empresa_sku ON gestao.produto (empresa_id, LOWER(sku));
CREATE UNIQUE INDEX IF NOT EXISTS uk_marca_empresa_nome ON gestao.marca (empresa_id, LOWER(nome));
CREATE UNIQUE INDEX IF NOT EXISTS uk_categoria_empresa_nome ON gestao.categoria (empresa_id, LOWER(nome));
CREATE UNIQUE INDEX IF NOT EXISTS uk_grupo_empresa_nome ON gestao.grupo (empresa_id, LOWER(nome));
CREATE UNIQUE INDEX IF NOT EXISTS uk_subgrupo_empresa_nome ON gestao.subgrupo (empresa_id, LOWER(nome));

INSERT INTO gestao.perfil (nome, criado_em, modificado_em)
SELECT 'ADMIN_SAAS', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM gestao.perfil WHERE nome = 'ADMIN_SAAS');

INSERT INTO gestao.perfil (nome, criado_em, modificado_em)
SELECT 'ADMIN_EMPRESA', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM gestao.perfil WHERE nome = 'ADMIN_EMPRESA');

INSERT INTO gestao.perfil (nome, criado_em, modificado_em)
SELECT 'GERENTE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM gestao.perfil WHERE nome = 'GERENTE');

INSERT INTO gestao.perfil (nome, criado_em, modificado_em)
SELECT 'OPERADOR', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM gestao.perfil WHERE nome = 'OPERADOR');

UPDATE gestao.perfil
SET nome = 'ADMIN_EMPRESA'
WHERE nome IN ('ROLE_ADMIN', 'ADMIN');

DELETE FROM gestao.usuario_perfil up
USING gestao.perfil p1, gestao.perfil p2
WHERE up.perfil_id = p2.id
  AND p1.nome = p2.nome
  AND p1.id < p2.id;

DELETE FROM gestao.perfil p2
USING gestao.perfil p1
WHERE p1.nome = p2.nome
  AND p1.id < p2.id;

CREATE UNIQUE INDEX IF NOT EXISTS uk_perfil_nome ON gestao.perfil (LOWER(nome));

COMMIT;

-- Opcional:
-- Para criar o primeiro ADMIN_SAAS sem inserir senha via SQL, configure:
-- APP_BOOTSTRAP_ADMIN_SAAS_NOME
-- APP_BOOTSTRAP_ADMIN_SAAS_USUARIO
-- APP_BOOTSTRAP_ADMIN_SAAS_SENHA
