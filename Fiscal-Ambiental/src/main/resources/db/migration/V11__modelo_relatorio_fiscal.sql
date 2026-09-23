-- Modelo do Relatório baseado no "Relatório Fiscal" de referência: assunto, atendimento,
-- processo administrativo e o texto da vistoria (corpo do relatório), além de fotos com
-- legenda anexadas durante a emissão.
ALTER TABLE relatorios ADD COLUMN assunto VARCHAR(255);
ALTER TABLE relatorios ADD COLUMN atendimento VARCHAR(255);
ALTER TABLE relatorios ADD COLUMN processo_administrativo VARCHAR(100);
ALTER TABLE relatorios ADD COLUMN texto_vistoria TEXT;

-- A tabela já existia desde V2 (esqueleto para armazenamento em arquivo — nome_arquivo/hash/
-- caminho_arquivo), mas nunca foi usada por nenhuma entidade e está vazia. Recria com o
-- desenho atual: a foto vai direto como data URI em base64.
DROP TABLE relatorio_imagens;

CREATE TABLE relatorio_imagens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_relatorio UUID NOT NULL REFERENCES relatorios(id) ON DELETE CASCADE,
    imagem_base64 TEXT NOT NULL,
    legenda TEXT,
    ordem INTEGER NOT NULL DEFAULT 0
);
