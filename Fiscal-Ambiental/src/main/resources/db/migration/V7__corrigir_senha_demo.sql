-- O hash gravado em V5 nunca correspondia de fato à senha "Demo@123" anunciada na tela de
-- login (era um hash de outra senha, de uma etapa anterior do desenvolvimento). Corrige os
-- três usuários demonstrativos para a senha realmente documentada na UI.
UPDATE usuarios
SET senha_hash = '$2b$12$tzZb/NDrmuHzuPB2BiPVi.wcfTC3gxdaW6LPWnvCJ35oN1fqKuuMm'
WHERE cpf IN ('00000000000', '11111111111', '22222222222');
