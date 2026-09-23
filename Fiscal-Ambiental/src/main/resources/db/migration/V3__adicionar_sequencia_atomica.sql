-- Tabela de controle de numeração --> separada por tipo de documento e ano
CREATE TABLE controle_sequencial (
                                     tipo VARCHAR(50) NOT NULL,
                                     ano INT NOT NULL,
                                     ultimo_numero BIGINT NOT NULL DEFAULT 0,
                                     PRIMARY KEY (tipo, ano)
);

-- Função Atômica
CREATE OR REPLACE FUNCTION gerar_numero_sequencial(p_tipo VARCHAR(50), p_ano INT)
    RETURNS BIGINT AS $$
DECLARE
    v_proximo_numero BIGINT;
BEGIN
    INSERT INTO controle_sequencial (tipo, ano, ultimo_numero)
    VALUES (p_tipo, p_ano, 0)
    ON CONFLICT (tipo, ano) DO NOTHING;

    UPDATE controle_sequencial
    SET ultimo_numero = ultimo_numero + 1
    WHERE tipo = p_tipo AND ano = p_ano
    RETURNING ultimo_numero INTO v_proximo_numero;

    RETURN v_proximo_numero;
END;
$$ LANGUAGE plpgsql;
