-- A inscrição do imóvel é o identificador único dele, mas pode ser digitada com ou sem pontos
-- (ex.: "012.045.0023.01" vs "012045002301") — sem normalização, essas duas formas seriam
-- tratadas como imóveis diferentes e a unicidade real do cadastro poderia ser burlada.
-- inscricao_normalizada guarda a versão sem pontos, usada para toda busca/verificação de
-- duplicidade; inscricao continua guardando o texto como foi digitado, para exibição.
ALTER TABLE imoveis ADD COLUMN inscricao_normalizada VARCHAR(50);

UPDATE imoveis SET inscricao_normalizada = REPLACE(inscricao, '.', '');

ALTER TABLE imoveis ALTER COLUMN inscricao_normalizada SET NOT NULL;
ALTER TABLE imoveis ADD CONSTRAINT uq_imoveis_inscricao_normalizada UNIQUE (inscricao_normalizada);
