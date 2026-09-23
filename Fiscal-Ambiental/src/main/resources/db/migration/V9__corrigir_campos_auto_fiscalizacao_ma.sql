-- V8 foi modelado a partir da categoria errada de referência (Auto de Infração, 1.2.MA).
-- O documento correto para o nosso Auto de Fiscalização é o 16.7° - Controle Processual:
-- Auto de Fiscalização/Meio Ambiente (categoria 1.9), que não tem os campos de documento de
-- referência/data-hora da infração/reincidência, mas tem um campo "Providências".
ALTER TABLE autos_fiscalizacao DROP COLUMN tipo_documento_referencia;
ALTER TABLE autos_fiscalizacao DROP COLUMN numero_documento_referencia;
ALTER TABLE autos_fiscalizacao DROP COLUMN data_infracao;
ALTER TABLE autos_fiscalizacao DROP COLUMN hora_infracao;
ALTER TABLE autos_fiscalizacao DROP COLUMN reincidente;
ALTER TABLE autos_fiscalizacao ADD COLUMN providencias TEXT;
