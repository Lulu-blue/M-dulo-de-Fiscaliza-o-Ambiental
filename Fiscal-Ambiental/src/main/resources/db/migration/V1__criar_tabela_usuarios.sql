CREATE TABLE usuarios (
                          id UUID PRIMARY KEY,
                          nome VARCHAR(255) NOT NULL,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          senha_hash VARCHAR(255) NOT NULL,
                          cargo VARCHAR(50) NOT NULL,
                          cpf VARCHAR(14) UNIQUE,
                          matricula VARCHAR(50) UNIQUE,
                          ativo BOOLEAN DEFAULT TRUE
);
