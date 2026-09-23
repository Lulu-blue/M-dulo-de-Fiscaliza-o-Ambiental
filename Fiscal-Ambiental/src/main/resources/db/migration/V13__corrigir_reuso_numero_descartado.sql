-- A versão de gerar_numero_sequencial criada em V4 tinha um bug: o RETURNING lia
-- numeros_descartados[1] DEPOIS do UPDATE já ter removido o primeiro elemento do array,
-- então sempre retornava NULL e caía no fallback de gerar um número novo — o número
-- descartado nunca era realmente reaproveitado. Corrige lendo o valor antes de remover.
CREATE OR REPLACE FUNCTION gerar_numero_sequencial(p_tipo VARCHAR(50), p_ano INT)
    RETURNS BIGINT AS $$
DECLARE
    v_proximo_numero BIGINT;
BEGIN
    INSERT INTO controle_sequencial (tipo, ano, ultimo_numero, numeros_descartados)
    VALUES (p_tipo, p_ano, 0, '{}')
    ON CONFLICT (tipo, ano) DO NOTHING;

    -- Trava a linha para evitar corrida entre chamadas concorrentes até o fim da transação.
    PERFORM 1 FROM controle_sequencial WHERE tipo = p_tipo AND ano = p_ano FOR UPDATE;

    SELECT numeros_descartados[1] INTO v_proximo_numero
    FROM controle_sequencial
    WHERE tipo = p_tipo AND ano = p_ano;

    IF v_proximo_numero IS NOT NULL THEN
        UPDATE controle_sequencial
        SET numeros_descartados = numeros_descartados[2:array_length(numeros_descartados, 1)]
        WHERE tipo = p_tipo AND ano = p_ano;
    ELSE
        UPDATE controle_sequencial
        SET ultimo_numero = ultimo_numero + 1
        WHERE tipo = p_tipo AND ano = p_ano
        RETURNING ultimo_numero INTO v_proximo_numero;
    END IF;

    RETURN v_proximo_numero;
END;
$$ LANGUAGE plpgsql;
