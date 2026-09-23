-- O Relatório de Vistoria normalmente é feito ANTES do Auto de Fiscalização (o auto costuma
-- ser lavrado depois, com base no que foi constatado na vistoria) — então o Imóvel precisa
-- poder existir sem um Contribuinte vinculado ainda. O Auto, quando emitido depois, completa
-- o cadastro com o responsável (ver ImovelService.buscarOuCriar).
ALTER TABLE imoveis ALTER COLUMN id_contribuinte DROP NOT NULL;
