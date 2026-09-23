# Ficha de Requisito

> **Norma de referência:** ISO/IEC/IEEE 29148:2018 (Seções 5.2.5 e 5.2.8)

---

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | `REQ-DESEMP-001` |
| **Nome** | `Numeração sequencial atômica sob concorrência` |
| **Tipo** | `Performance / Confiabilidade` |
| **Prioridade** | `Essencial` |
| **Status** | `Implementado` |
| **Versão** | `1.1` |

---

## Descrição

### Texto do Requisito

O sistema deve gerar, para cada Auto de Fiscalização e para cada Relatório, um `numeroSequencial` único por `ano`, de modo que duas ou mais emissões concorrentes do mesmo tipo de documento, ocorridas no mesmo instante, nunca recebam o mesmo número.

### Condições de Aplicação

- Condição 1: Aplica-se separadamente às sequências de `autos_fiscalizacao` e de `relatorios` (sequências independentes entre si).
- Condição 2: A contagem reinicia a cada novo `ano`.
- Condição 3: Aplica-se sob concorrência de múltiplas requisições simultâneas ao mesmo serviço de numeração.

---

## Rastreabilidade

| Campo | Valor |
|-------|-------|
| **Fonte (Source)** | `SRS_MODULO_FISCALIZACAO_AMBIENTAL_v1.0.md`, Seções 3.4 e 3.7 |
| **Requisito Pai** | `REQ-FUNC-004`, `REQ-FUNC-005` |
| **Requisitos Filhos** | — |
| **Casos de Uso / Histórias Relacionadas** | UC-EMITIR-AUTO, UC-EMITIR-RELATORIO |

---

## Justificativa (Rationale)

É o diferencial técnico central do projeto, descrito no plano como prevenção de condições de corrida (race conditions), e citado explicitamente no roteiro de estudos da vaga como demonstração de "maturidade em banco de dados e lógica de backend".

---

## Critérios de Verificação e Aceitação

### Método de Verificação
- [ ] Inspeção / Revisão
- [x] Teste
- [ ] Demonstração
- [x] Simulação (20 requisições HTTP reais disparadas em paralelo via `curl ... &` + `wait`, não apenas leitura de código)

### Critérios de Aceitação (Gherkin / BDD recomendado)
```gherkin
Funcionalidade: Numeração sequencial atômica
  Cenário: Duas emissões concorrentes de Auto no mesmo ano
    Dado duas requisições de emissão de Auto de Fiscalização disparadas simultaneamente
    Quando ambas são processadas pelo NumeracaoService
    Então cada uma recebe um numeroSequencial distinto, sem colisão, dentro do mesmo ano

  Cenário: Sequências independentes por tipo de documento
    Dado que já existe um Auto com numeroSequencial 1 no ano corrente
    Quando um Relatório é emitido no mesmo ano
    Então o Relatório pode receber numeroSequencial 1 sem conflito, pois a sequência é independente da de Autos
```

### Casos de Teste Associados
| ID do Teste | Descrição | Resultado Esperado | Resultado Obtido |
|-------------|-----------|--------------------|-------------------|
| TEST-001 | Disparar 20 requisições **reais e simultâneas** (`curl` em paralelo, `wait`) de emissão de Auto no mesmo ano, referenciando Contribuinte/Imóvel pré-existentes (para contornar o gap de `REQ-FUNC-004`) | 20 números sequenciais distintos, sem repetição | ✅ **Confirmado sob concorrência real**: os 20 autos criados receberam os números 2 a 21 (sequência já estava em 1 por um teste anterior), sem nenhuma duplicata — conferido tanto nas respostas HTTP quanto por `GROUP BY ... HAVING count(*) > 1` direto no banco (zero linhas) |
| TEST-002 | Disparar N requisições concorrentes de emissão de Relatório no mesmo ano | N números sequenciais distintos, sem repetição | ⏳ Não testado sob concorrência real nesta rodada (só emissão isolada, TEST-001 de `REQ-FUNC-005`); o mecanismo é o mesmo `NumeracaoService`/função de banco usado no TEST-001 acima, então o resultado é esperado ser igual, mas não foi executado com múltiplas requisições simultâneas de Relatório especificamente |
| TEST-003 | Emitir documentos em anos diferentes | Reinício da contagem a cada ano | ⏳ Não testado (exigiria adulterar a data do servidor ou aguardar a virada de ano) — comportamento inferido pela leitura da função SQL (`gerar_numero_sequencial(tipo, ano)`), não confirmado empiricamente |

---

## Análise de Conformidade com a Norma

| Característica | Atende? | Observações |
|----------------|---------|-------------|
| **Necessary (Necessário)** | [x] Sim [ ] Não | Sem esta garantia, dois documentos oficiais poderiam ter o mesmo número, invalidando-os. |
| **Appropriate (Apropriado)** | [x] Sim [ ] Não | Não prescreve a técnica exata (sequence, lock, função de banco), apenas a garantia observável. |
| **Unambiguous (Não ambíguo)** | [x] Sim [ ] Não | "Nunca recebam o mesmo número" é uma condição objetiva e testável. |
| **Complete (Completo)** | [x] Sim [ ] Não | Autocontido. |
| **Singular (Singular)** | [x] Sim [ ] Não | Trata exclusivamente da unicidade sob concorrência. |
| **Feasible (Factível)** | [x] Sim [ ] Não | Já implementado (`NumeracaoService`, migrações V3/V4). |
| **Verifiable (Verificável)** | [x] Sim [ ] Não | Verificado por teste de concorrência real (TEST-001, 20 requisições simultâneas, zero colisões). |
| **Correct (Correto)** | [x] Sim [ ] Não | Reflete o mecanismo descrito nas migrações Flyway V3/V4, e o comportamento observado bate com o esperado. |
| **Conforming (Conforme)** | [x] Sim [ ] Não | Segue o template. |

---

## Informações Complementares

### Restrições e Dependências
- Depende do mecanismo atômico implementado em `V3__adicionar_sequencia_atomica.sql` e do tratamento de descarte em `V4__adicionar_descarte_numeracao.sql`.
- Depende, na prática, de `REQ-FUNC-004`/`REQ-FUNC-005` para ser exercitado via API pública — esses dois têm gaps que impedem o fluxo real (frontend) de chegar a emitir um documento. O teste desta ficha foi feito inserindo Contribuinte/Imóvel diretamente no banco para isolar e validar **apenas** a numeração, que funciona corretamente.

### Notas e Suposições
- Assume-se que o mecanismo de numeração é implementado a nível de banco de dados (não em memória da aplicação), condição necessária para garantir atomicidade mesmo com múltiplas instâncias do backend em execução.

### Anexos / Referências
- `Fiscal-Ambiental/src/main/resources/db/migration/V3__adicionar_sequencia_atomica.sql`
- `Fiscal-Ambiental/src/main/resources/db/migration/V4__adicionar_descarte_numeracao.sql`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/service/NumeracaoService.java`

---

## Histórico de Alterações

| Versão | Data | Autor | Alteração |
|--------|------|-------|-----------|
| 1.0 | 10/09/2026 | Luiza | Criação inicial |
| 1.1 | 13/09/2026 | Luiza | Testado sob concorrência real pela primeira vez: 20 requisições HTTP simultâneas de emissão de Auto, zero colisões de `numeroSequencial` (confirmado nas respostas e no banco). TEST-002 (concorrência de Relatório) e TEST-003 (virada de ano) permanecem não testados empiricamente. |
