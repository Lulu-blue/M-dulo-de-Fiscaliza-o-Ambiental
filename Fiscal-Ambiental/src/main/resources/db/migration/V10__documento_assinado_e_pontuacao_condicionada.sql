-- A pontuação de produtividade passa a depender do anexo do documento (Auto/Relatório) já
-- impresso, assinado e digitalizado pelo fiscal — não é mais atribuída automaticamente na
-- emissão. Remover o anexo dentro de 24h também remove a pontuação (ver camada de serviço).
ALTER TABLE autos_fiscalizacao ADD COLUMN documento_assinado BYTEA;
ALTER TABLE autos_fiscalizacao ADD COLUMN documento_assinado_nome VARCHAR(255);
ALTER TABLE autos_fiscalizacao ADD COLUMN documento_assinado_content_type VARCHAR(100);
ALTER TABLE autos_fiscalizacao ADD COLUMN documento_assinado_enviado_em TIMESTAMP;

ALTER TABLE relatorios ADD COLUMN documento_assinado BYTEA;
ALTER TABLE relatorios ADD COLUMN documento_assinado_nome VARCHAR(255);
ALTER TABLE relatorios ADD COLUMN documento_assinado_content_type VARCHAR(100);
ALTER TABLE relatorios ADD COLUMN documento_assinado_enviado_em TIMESTAMP;
ALTER TABLE relatorios ADD COLUMN id_criador UUID REFERENCES usuarios(id);

-- Registros existentes ainda não têm o documento assinado anexado (coluna nova), então não
-- fazem jus à pontuação até que o fiscal anexe.
UPDATE autos_fiscalizacao SET pontos_produtividade = 0;
UPDATE relatorios SET pontos_produtividade_base = 0;
