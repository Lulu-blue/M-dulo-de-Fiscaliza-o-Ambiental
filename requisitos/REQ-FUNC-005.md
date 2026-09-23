# Ficha de Requisito

> **Norma de referência:** ISO/IEC/IEEE 29148:2018 (Seções 5.2.5 e 5.2.8)

---

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | `REQ-FUNC-005` |
| **Nome** | `Emissão de Relatório de Vistoria pelo Fiscal` |
| **Tipo** | `Funcional` |
| **Prioridade** | `Essencial` |
| **Status** | `Implementado` |
| **Versão** | `1.4` |

---

## Descrição

### Texto do Requisito

O sistema deve permitir que um Fiscal registre um Relatório de vistoria vinculado a uma Demanda e a um Imóvel, podendo associar um ou mais fiscais responsáveis, recebendo um número sequencial próprio e único para o ano corrente, independente da numeração dos Autos de Fiscalização.

### Condições de Aplicação

- Condição 1: A requisição é enviada por um usuário autenticado com cargo `FISCAL`.
- Condição 2: A Demanda referenciada está atribuída ao Fiscal autenticado. **Implementado em 13/09/2026** — mesma correção de `REQ-FUNC-004`.
- Condição 3: A requisição é enviada a `POST /api/fiscal/relatorios`. ~~(ou `POST /api/relatorios`)~~ — rota alternativa removida em `REQ-SEG-001` v1.2.
- Condição 4: O Imóvel referenciado precisa já existir no banco, encontrado por `id` **ou por inscrição** (o Relatório não coleta dados completos de imóvel, então não cadastra um novo — decisão de design registrada em Restrições e Dependências). **Corrigido em 13/09/2026** — o frontend agora coleta a inscrição num campo próprio no modal, em vez de enviar o ID da Demanda por engano.

---

## Rastreabilidade

| Campo | Valor |
|-------|-------|
| **Fonte (Source)** | `SRS_MODULO_FISCALIZACAO_AMBIENTAL_v1.0.md`, Seções 2.2, 3.1, 3.2 e 3.5 |
| **Requisito Pai** | `REQ-FUNC-003` |
| **Requisitos Filhos** | `REQ-DESEMP-001` |
| **Casos de Uso / Histórias Relacionadas** | UC-EMITIR-RELATORIO |

---

## Justificativa (Rationale)

O Relatório representa um segundo tipo de documento oficial do domínio de fiscalização ambiental, com sequência numérica independente do Auto e suporte a múltiplos fiscais (relação N:N), conforme modelado em `relatorio_fiscais`.

---

## Critérios de Verificação e Aceitação

### Método de Verificação
- [ ] Inspeção / Revisão
- [x] Teste
- [ ] Demonstração
- [ ] Análise
- [ ] Simulação

### Critérios de Aceitação (Gherkin / BDD recomendado)
```gherkin
Funcionalidade: Emissão de Relatório de Vistoria
  Cenário: Fiscal emite Relatório para demanda atribuída a ele
    Dado uma demanda EM_ANDAMENTO atribuída ao fiscal autenticado
    Quando ele envia POST /api/fiscal/relatorios com imóvel, data/hora de vistoria e fiscais participantes
    Então o sistema cria o Relatório com numeroSequencial único para o ano corrente, independente da sequência de Autos

  Cenário: Numeração de Relatório não colide com numeração de Auto
    Dado que já existe um Auto de Fiscalização com numeroSequencial 1 no ano corrente
    Quando um Relatório é emitido no mesmo ano
    Então o Relatório pode igualmente receber numeroSequencial 1, pois as sequências são independentes
```

### Casos de Teste Associados
| ID do Teste | Descrição | Resultado Esperado | Resultado Obtido |
|-------------|-----------|--------------------|-------------------|
| TEST-001 | Emitir Relatório exatamente como o frontend enviava antes da correção (`imovel: {id: demandaSelecionada.id}`) | — (documentação do bug original) | ❌→✅ **Corrigido**: o bug de referência foi eliminado — o frontend agora envia `imovel: {inscricao: ...}` a partir de um campo próprio no formulário, não mais o ID da Demanda |
| TEST-002 | Emitir Relatório referenciando um Imóvel **já existente** por inscrição, e uma lista de 2 fiscais | Status 200, Relatório criado com `numeroSequencial`/`ano`, e registros em `relatorio_fiscais` para os dois | ✅ Confirmado — `numeroSequencial: 1` para 2026, dois fiscais persistidos corretamente na tabela associativa |
| TEST-003 | Confirmar que a sequência de Relatório é independente da de Auto | Ambos podem valer `1` no mesmo ano sem conflito | ✅ Confirmado — um Auto e um Relatório emitidos no mesmo teste de sessão, ambos `numeroSequencial: 1`/`ano: 2026`, sem colisão |
| TEST-004 | Emitir Relatório com inscrição de Imóvel **inexistente** | Erro claro, não deve travar/crashar | ✅ Confirmado — `400` com `{"message": "Nenhum imóvel cadastrado com a inscrição '...'. Emita um Auto de Fiscalização para este imóvel primeiro, ou confira a inscrição informada."}` |
| TEST-005 | Emitir Relatório pela **tela real** do Fiscal: selecionar demanda, abrir modal, preencher a inscrição (de um imóvel já cadastrado por um Auto anterior) e submeter | Relatório criado, modal fecha, sem erro no console | ✅ Confirmado via Chrome headless — `numeroSequencial: 3` persistido corretamente, vinculado ao imóvel certo |
| TEST-006 | Fiscal tenta emitir Relatório para demanda de **outro** fiscal | Erro de validação/permissão | ✅ **Corrigido** — responde `403` com `{"message": "Esta demanda não está atribuída a você."}`. Testado: Fiscal Mariana bloqueada ao tentar para uma demanda do Fiscal Carlos |
| TEST-007 (regressão) | Fiscal dono da demanda emite Relatório normalmente, após a correção do TEST-006 | Status 200 | ✅ Confirmado — sem regressão |

---

## Análise de Conformidade com a Norma

| Característica | Atende? | Observações |
|----------------|---------|-------------|
| **Necessary (Necessário)** | [x] Sim [ ] Não | Documento previsto explicitamente no modelo de dados do plano do projeto. |
| **Appropriate (Apropriado)** | [x] Sim [ ] Não | Não define layout do relatório, apenas o comportamento de emissão. |
| **Unambiguous (Não ambíguo)** | [x] Sim [ ] Não | Distingue claramente a sequência de Relatório da de Auto. |
| **Complete (Completo)** | [x] Sim [ ] Não | Autocontido. |
| **Singular (Singular)** | [x] Sim [ ] Não | Trata apenas da emissão do Relatório. |
| **Feasible (Factível)** | [x] Sim [ ] Não | O fluxo real, como o frontend envia hoje (inscrição do imóvel coletada em campo próprio), funciona de ponta a ponta — confirmado pela API e pela tela real (TEST-002, TEST-005). |
| **Verifiable (Verificável)** | [x] Sim [ ] Não | Verificável pela unicidade do `numeroSequencial` por ano, pelos registros em `relatorio_fiscais`, e pelo erro claro quando a inscrição não existe (TEST-004). |
| **Correct (Correto)** | [x] Sim [ ] Não | A implementação agora reflete integralmente o texto do requisito, incluindo a checagem de posse da Demanda (TEST-006/007). |
| **Conforming (Conforme)** | [x] Sim [ ] Não | Segue o template. |

---

## Informações Complementares

### Restrições e Dependências
- Depende de `REQ-FUNC-003` (Demanda já delegada) — gap em aberto.
- Depende de `REQ-DESEMP-001` (numeração sequencial atômica, aplicada de forma independente por tipo de documento) — **essa parte funciona**, validada sob concorrência real.
- ~~Gap crítico e bloqueante: `emitirRelatorioSubmit()` enviava o ID da Demanda no lugar do ID do Imóvel...~~ **Corrigido em 13/09/2026.** Adicionado `relatorioImovelForm` (campo "Inscrição do Imóvel") ao modal de emissão de Relatório, com uma dica de texto explicando que precisa ser a inscrição de um imóvel já cadastrado. `emitirRelatorioSubmit()` agora envia `imovel: { inscricao: this.relatorioImovelForm.inscricao }`.
- **Decisão de design registrada:** ao contrário do Auto (que coleta dados completos de Contribuinte/Imóvel e pode cadastrar um novo), o Relatório só referencia um Imóvel **já existente** — `ImovelService.buscarExistente()` procura por `id` ou por inscrição e retorna erro claro (`400`) se não encontrar, em vez de tentar criar um Imóvel sem Contribuinte associado (o que violaria a obrigatoriedade dessa referência). Na prática, o Imóvel de um Relatório normalmente já foi cadastrado antes por um Auto de Fiscalização para o mesmo local.
- Erros de validação (imóvel não encontrado, etc.) agora retornam `400` com `{"message": "..."}` — `FiscalController.emitirRelatorio()` passou a capturar `IllegalArgumentException` explicitamente, evitando o mascaramento como `403` via `/error`.
- ~~Gap real: `FiscalController.emitirRelatorio()` não verifica posse da Demanda...~~ **Corrigido em 13/09/2026** — mesma correção de `REQ-FUNC-004`: `validarPosseDaDemanda()` (método privado compartilhado em `FiscalController`) agora bloqueia com `403` qualquer emissão para uma Demanda não atribuída ao Fiscal autenticado.

### Notas e Suposições
- ~~Assume-se que a associação de imagens ao Relatório (`relatorio_imagens`) segue o mesmo mecanismo de deduplicação por hash descrito em `REQ-FUNC-006` — não verificado: não existe nenhum controller/service para `relatorio_imagens`; a tabela existe no banco mas não é usada por nenhum endpoint.~~ **Desatualizado — corrigido em 22/09/2026.** A tabela `relatorio_imagens` original (desde `V2`) de fato nunca chegou a ser usada e foi **recriada** em `V11__modelo_relatorio_fiscal.sql` com um desenho novo (`imagem_base64` em vez de referência a arquivo em disco). `Relatorio.imagens` (`List<RelatorioImagem>`) está mapeado e em uso — o Relatório agora suporta fotos com legenda anexadas na emissão. Diferente do Anexo, essas imagens **não passam pelo mecanismo de deduplicação por hash** de `REQ-FUNC-006`/`REQ-DESEMP-002` — são armazenadas como base64 embutido no próprio documento, sem checagem de hash prévia. Isso é uma divergência real de comportamento entre os dois mecanismos de mídia do sistema (Anexo vs. imagem de Relatório), não coberta por nenhum requisito formal ainda (registrado como TBD-010 no SRS, Apêndice C).

### Anexos / Referências
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/FiscalController.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/service/RelatorioService.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/service/ImovelService.java` (novo)
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/model/Relatorio.java`
- `fiscal-ambiental-frontend/src/app/components/fiscal-dashboard/fiscal-dashboard.component.ts` / `.html`

---

## Histórico de Alterações

| Versão | Data | Autor | Alteração |
|--------|------|-------|-----------|
| 1.0 | 10/09/2026 | Luiza | Criação inicial |
| 1.1 | 13/09/2026 | Luiza | Testado ao vivo pela primeira vez. Encontrado gap crítico e bloqueante, com dupla causa: (a) `fiscal-dashboard.component.ts` envia o ID da Demanda no lugar do ID do Imóvel; (b) não há endpoint para criar Imóvel. Isolando com um Imóvel pré-existente e ID correto, a emissão e a relação N:N com fiscais funcionam. Confirmado também que a sequência de Relatório é independente da de Auto. Status rebaixado para "Em revisão". |
| 1.2 | 13/09/2026 | Luiza | Gap crítico corrigido: bug de referência errada eliminado (frontend agora tem campo próprio de inscrição do imóvel no modal) e `RelatorioService` passou a resolver o Imóvel via `ImovelService.buscarExistente()`, com erro `400` claro se não encontrado. Testado com sucesso via API e pela **tela real** (Chrome headless). Status permanece "Em revisão" só por causa da checagem de posse da Demanda (mesmo gap de `REQ-FUNC-004`), que segue em aberto e não fazia parte do pedido desta correção. |
| 1.3 | 13/09/2026 | Luiza | Último gap corrigido: `FiscalController.emitirRelatorio()` agora usa o mesmo `validarPosseDaDemanda()` criado para `REQ-FUNC-004`, retornando `403` claro quando a Demanda não pertence ao Fiscal autenticado. TEST-006/007 confirmados, sem regressão. Status finalmente "Implementado". |
| 1.4 | 22/09/2026 | Luiza | Verificação de atualidade: a nota "não verificado" sobre `relatorio_imagens` estava desatualizada — a tabela foi recriada em `V11` e está em uso de verdade (upload de fotos com legenda no Relatório). Identificada e registrada uma divergência real (fotos de Relatório não passam por deduplicação de hash, diferente do Anexo) como novo item TBD-010 no SRS. |
