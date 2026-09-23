-- coluna de array nova
ALTER TABLE controle_sequencial 
ADD COLUMN numeros_descartados BIGINT[] DEFAULT '{}';

-- Atualizar para usar o array de descartados
CREATE OR REPLACE FUNCTION gerar_numero_sequencial(p_tipo VARCHAR(50), p_ano INT)
    RETURNS BIGINT AS $$
DECLARE
    v_proximo_numero BIGINT;
BEGIN
    INSERT INTO controle_sequencial (tipo, ano, ultimo_numero, numeros_descartados)
    VALUES (p_tipo, p_ano, 0, '{}')
    ON CONFLICT (tipo, ano) DO NOTHING;

    -- Primeiro número da lista
    UPDATE controle_sequencial
    SET numeros_descartados = numeros_descartados[2:array_length(numeros_descartados, 1)]
    WHERE tipo = p_tipo AND ano = p_ano AND array_length(numeros_descartados, 1) > 0
    RETURNING numeros_descartados[1] INTO v_proximo_numero;

    -- Se estiver vazio gera um número novo
    IF v_proximo_numero IS NULL THEN
        UPDATE controle_sequencial
        SET ultimo_numero = ultimo_numero + 1
        WHERE tipo = p_tipo AND ano = p_ano
        RETURNING ultimo_numero INTO v_proximo_numero;
    END IF;

    RETURN v_proximo_numero;
END;
$$ LANGUAGE plpgsql;

-- Nova função para descartar os numeros
CREATE OR REPLACE FUNCTION descartar_numero_sequencial(p_tipo VARCHAR(50), p_ano INT, p_numero BIGINT)
    RETURNS VOID AS $$
BEGIN
    UPDATE controle_sequencial
    SET numeros_descartados = array_append(numeros_descartados, p_numero)
    WHERE tipo = p_tipo AND ano = p_ano;
END;
$$ LANGUAGE plpgsql;
