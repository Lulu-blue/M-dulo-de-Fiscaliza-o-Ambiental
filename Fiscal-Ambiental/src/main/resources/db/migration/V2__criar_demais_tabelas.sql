-- 1. Contribuintes
CREATE TABLE contribuintes (
                               id UUID PRIMARY KEY,
                               nome VARCHAR(255) NOT NULL,
                               cpf_cnpj VARCHAR(18) UNIQUE,
                               rua VARCHAR(255),
                               numero VARCHAR(50),
                               bairro VARCHAR(100),
                               municipio VARCHAR(100),
                               cep VARCHAR(20)
);

-- 2. Imóveis
CREATE TABLE imoveis (
                         id UUID PRIMARY KEY,
                         inscricao VARCHAR(100),
                         codigo_reduzido VARCHAR(50),
                         rua VARCHAR(255),
                         numero VARCHAR(50),
                         bairro VARCHAR(100),
                         cep VARCHAR(20),
                         id_contribuinte UUID REFERENCES contribuintes(id)
);

-- 3. Demandas (A Tarefa Base)
CREATE TABLE demandas (
                          id UUID PRIMARY KEY,
                          titulo VARCHAR(255) NOT NULL,
                          descricao TEXT,
                          status VARCHAR(50) DEFAULT 'PENDENTE',
                          id_gestor_criador UUID REFERENCES usuarios(id),
                          id_fiscal_atribuido UUID REFERENCES usuarios(id),
                          data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          data_conclusao TIMESTAMP
);

-- 4. Autos de Fiscalização
CREATE TABLE autos_fiscalizacao (
                                    id UUID PRIMARY KEY,
                                    numero_sequencial BIGINT NOT NULL,
                                    ano INT NOT NULL,
                                    id_demanda UUID REFERENCES demandas(id),
                                    id_criador UUID REFERENCES usuarios(id),
                                    id_contribuinte UUID REFERENCES contribuintes(id),
                                    id_imovel UUID REFERENCES imoveis(id),
                                    processo_administrativo VARCHAR(100),
                                    irregularidades_constatadas TEXT,
                                    prazo_defesa INT,
                                    dados_vistoria TEXT,
                                    pontos_produtividade INT,
                                    data_geracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    CONSTRAINT uk_auto_numero_ano UNIQUE (numero_sequencial, ano)
);

-- 5. Relatórios
CREATE TABLE relatorios (
                            id UUID PRIMARY KEY,
                            numero_sequencial BIGINT NOT NULL,
                            ano INT NOT NULL,
                            id_demanda UUID REFERENCES demandas(id),
                            id_imovel UUID REFERENCES imoveis(id),
                            data_hora_vistoria TIMESTAMP,
                            pontos_produtividade_base INT,
                            data_geracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT uk_relatorio_numero_ano UNIQUE (numero_sequencial, ano)
);

-- 6. Relacionamento N:N (Relatório e Fiscais)
CREATE TABLE relatorio_fiscais (
                                   id_relatorio UUID REFERENCES relatorios(id),
                                   id_fiscal UUID REFERENCES usuarios(id),
                                   PRIMARY KEY (id_relatorio, id_fiscal)
);

-- 7. Anexos (PDFs Gerais)
CREATE TABLE anexos (
                        id UUID PRIMARY KEY,
                        id_demanda UUID REFERENCES demandas(id),
                        nome_arquivo VARCHAR(255) NOT NULL,
                        hash_sha256 VARCHAR(255) NOT NULL,
                        tamanho_bytes BIGINT,
                        data_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Imagens dos Relatórios (Onde usaremos as legendas)
CREATE TABLE relatorio_imagens (
                                   id UUID PRIMARY KEY,
                                   id_relatorio UUID REFERENCES relatorios(id),
                                   nome_arquivo VARCHAR(255) NOT NULL,
                                   hash_sha256 VARCHAR(255) NOT NULL,
                                   caminho_arquivo VARCHAR(255),
                                   legenda TEXT,
                                   ordem INT
);
