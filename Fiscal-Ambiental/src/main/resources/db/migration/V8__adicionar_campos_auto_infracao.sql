-- Campos do modelo de Auto de Infração/Fiscalização Ambiental (dispositivos legais
-- transgredidos, penalidades, dados da infração e esquema de testemunhas).
ALTER TABLE autos_fiscalizacao ADD COLUMN tipo_documento_referencia VARCHAR(50);
ALTER TABLE autos_fiscalizacao ADD COLUMN numero_documento_referencia VARCHAR(100);
ALTER TABLE autos_fiscalizacao ADD COLUMN data_infracao DATE;
ALTER TABLE autos_fiscalizacao ADD COLUMN hora_infracao VARCHAR(10);
ALTER TABLE autos_fiscalizacao ADD COLUMN reincidente BOOLEAN;
ALTER TABLE autos_fiscalizacao ADD COLUMN dispositivos_legais TEXT;
ALTER TABLE autos_fiscalizacao ADD COLUMN penalidades TEXT;
ALTER TABLE autos_fiscalizacao ADD COLUMN tem_testemunhas BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE autos_fiscalizacao ADD COLUMN nome_testemunha_1 VARCHAR(255);
ALTER TABLE autos_fiscalizacao ADD COLUMN cpf_testemunha_1 VARCHAR(20);
ALTER TABLE autos_fiscalizacao ADD COLUMN nome_testemunha_2 VARCHAR(255);
ALTER TABLE autos_fiscalizacao ADD COLUMN cpf_testemunha_2 VARCHAR(20);
