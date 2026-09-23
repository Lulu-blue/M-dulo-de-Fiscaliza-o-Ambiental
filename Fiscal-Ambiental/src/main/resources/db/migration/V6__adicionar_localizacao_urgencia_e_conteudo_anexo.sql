-- Campos opcionais de Demanda: localização informada pelo Gestor e nível de urgência
ALTER TABLE demandas ADD COLUMN localizacao VARCHAR(255);
ALTER TABLE demandas ADD COLUMN urgencia VARCHAR(20);

-- Conteúdo real do arquivo do anexo (até aqui só se guardava metadados: nome/hash/tamanho).
-- Necessário para que o anexo possa ser efetivamente visualizado/baixado depois, não só
-- registrado para fins de deduplicação por hash.
ALTER TABLE anexos ADD COLUMN conteudo BYTEA;
ALTER TABLE anexos ADD COLUMN content_type VARCHAR(100);

-- Quem enviou o anexo (Gestor ou Fiscal) — antes só era possível anexar como Fiscal.
ALTER TABLE anexos ADD COLUMN id_enviado_por UUID REFERENCES usuarios(id);
