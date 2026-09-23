# Ficha de Requisito

> **Norma de referência:** ISO/IEC/IEEE 29148:2018 (Seções 5.2.5 e 5.2.8)

---

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | `REQ-FUNC-004` |
| **Nome** | `Emissão de Auto de Fiscalização pelo Fiscal` |
| **Tipo** | `Funcional` |
| **Prioridade** | `Essencial` |
| **Status** | `Implementado` |
| **Versão** | `1.4` |

---

## Descrição

### Texto do Requisito

O sistema deve permitir que um Fiscal, a partir de uma Demanda atribuída a ele, registre um Auto de Fiscalização vinculado a um Contribuinte e a um Imóvel, recebendo um número sequencial único para o ano corrente.

### Condições de Aplicação

- Condição 1: A requisição é enviada por um usuário autenticado com cargo `FISCAL`.
- Condição 2: A Demanda referenciada está atribuída ao Fiscal solicitante. **Implementado em 13/09/2026** — ver Restrições e Dependências.
- Condição 3: A requisição é enviada a `POST /api/fiscal/autos`. ~~(ou `POST /api/autos`)~~ — a rota alternativa foi removida em `REQ-SEG-001` v1.2.
- Condição 4: O Contribuinte e o Imóvel podem ser referenciados por `id` (já existentes) **ou** enviados como dados novos (nome+CPF/CNPJ; inscrição+rua) — o backend resolve por CPF/CNPJ e por inscrição, reaproveitando o registro se já existir ou cadastrando um novo caso contrário. **Implementado em 13/09/2026** — ver Restrições e Dependências.

---

## Rastreabilidade

| Campo | Valor |
|-------|-------|
| **Fonte (Source)** | `SRS_MODULO_FISCALIZACAO_AMBIENTAL_v1.0.md`, Seções 2.2, 3.1, 3.2 e 3.5 |
| **Requisito Pai** | `REQ-FUNC-003` |
| **Requisitos Filhos** | `REQ-DESEMP-001` |
| **Casos de Uso / Histórias Relacionadas** | UC-EMITIR-AUTO |

---

## Justificativa (Rationale)

É a etapa central do fluxo de fiscalização: transforma a vistoria em um documento oficial e numerado, atendendo ao objetivo do plano de projeto de tratar a tarefa como um "processo oficial", diferencial destacado no roteiro de estudos da vaga.

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
Funcionalidade: Emissão de Auto de Fiscalização
  Cenário: Fiscal emite Auto para demanda atribuída a ele
    Dado uma demanda EM_ANDAMENTO atribuída ao fiscal autenticado
    Quando ele envia POST /api/fiscal/autos com dados de contribuinte, imóvel e irregularidades
    Então o sistema cria o Auto com numeroSequencial único para o ano corrente

  Cenário: Fiscal tenta emitir Auto para demanda de outro fiscal
    Dado uma demanda atribuída a outro fiscal
    Quando ele envia POST /api/fiscal/autos referenciando essa demanda
    Então o sistema rejeita a operação
```

### Casos de Teste Associados
| ID do Teste | Descrição | Resultado Esperado | Resultado Obtido |
|-------------|-----------|--------------------|-------------------|
| TEST-001 | Emitir Auto exatamente como o frontend envia (contribuinte/imóvel como objetos novos, sem `id`, vindos do formulário) | Status 200, Auto criado | ✅ **Corrigido** — `Contribuinte`/`Imóvel` agora são resolvidos por `ContribuinteService`/`ImovelService` antes de salvar o Auto. Testado com dados 100% novos ("Maria da Silva", CPF `12345678900`, inscrição `INSC-E2E-001`): `200`, Auto criado, Contribuinte e Imóvel aparecem completos na resposta (não são mais stubs com campos `null`) |
| TEST-002 | Emitir Auto referenciando um Contribuinte e um Imóvel **já existentes** | Status 200, Auto criado com `numeroSequencial` e `ano` | ✅ Confirmado — a numeração continua correta (`numeroSequencial: 1`, depois testado até `21` em concorrência — ver `REQ-DESEMP-001`) |
| TEST-003 | Emitir um **segundo** Auto reaproveitando o mesmo CPF/CNPJ e a mesma inscrição do TEST-001 | Não deve criar um segundo Contribuinte/Imóvel — deve reaproveitar os já existentes (evita violar a constraint `UNIQUE` de `cpf_cnpj`/`inscricao`) | ✅ Confirmado — o segundo Auto (`numeroSequencial: 24`) referenciou exatamente o mesmo `contribuinte.id`/`imovel.id` do primeiro; `SELECT count(*)` no banco confirmou 1 único registro de cada |
| TEST-004 | Emitir Auto pela **tela real** do Fiscal (não só API): preencher o formulário completo (processo, irregularidades, prazo, contribuinte, imóvel) e submeter | Auto criado, modal fecha, sem erro no console | ✅ Confirmado via Chrome headless simulando digitação real nos campos — dado persistido corretamente no banco (`PA-UI-TESTE-002`, `numeroSequencial: 25`) |
| TEST-005 | Emitir Auto para demanda de **outro** fiscal (não atribuída a quem está autenticado) | Erro de validação/permissão | ✅ **Corrigido** — responde `403` com `{"message": "Esta demanda não está atribuída a você."}`. Testado: Fiscal Mariana tentou emitir para uma demanda atribuída ao Fiscal Carlos, bloqueado |
| TEST-006 (regressão) | Fiscal dono da demanda emite Auto normalmente, após a correção do TEST-005 | Status 200 | ✅ Confirmado — nenhuma regressão no fluxo legítimo |
| TEST-007 | Emitir um **segundo** Auto para a **mesma** Demanda que já possui um Auto | Rejeitado | ✅ Confirmado — `AutoFiscalizacaoService.gerarAuto()` verifica `autoFiscalizacaoRepository.existsByDemandaId()` e responde `400` com `{"message": "Esta demanda já possui um Auto de Fiscalização emitido. Edite o Auto existente em vez de criar outro."}` |

---

## Análise de Conformidade com a Norma

| Característica | Atende? | Observações |
|----------------|---------|-------------|
| **Necessary (Necessário)** | [x] Sim [ ] Não | Etapa 3 do fluxo principal descrito no plano do projeto. |
| **Appropriate (Apropriado)** | [x] Sim [ ] Não | Não especifica algoritmo de numeração (delegado a `REQ-DESEMP-001`). |
| **Unambiguous (Não ambíguo)** | [x] Sim [ ] Não | Ator, pré-condição e resultado únicos. |
| **Complete (Completo)** | [x] Sim [ ] Não | Autocontido, referenciando entidades já definidas na Seção 3.5 do SRS. |
| **Singular (Singular)** | [x] Sim [ ] Não | Trata apenas da emissão do Auto (não do Relatório). |
| **Feasible (Factível)** | [x] Sim [ ] Não | O fluxo completo, como o frontend realmente envia (Contribuinte/Imóvel novos, sem ID), agora funciona de ponta a ponta — confirmado pela API e pela tela real (TEST-001, TEST-004). |
| **Verifiable (Verificável)** | [x] Sim [ ] Não | Verificável pela existência e unicidade do `numeroSequencial`, pela ausência de duplicação de Contribuinte/Imóvel (TEST-003), e pela persistência correta observada pela tela real. |
| **Correct (Correto)** | [x] Sim [ ] Não | A implementação agora reflete integralmente o texto do requisito, incluindo a checagem de posse da Demanda (TEST-005/006). |
| **Conforming (Conforme)** | [x] Sim [ ] Não | Segue o template. |

---

## Informações Complementares

### Restrições e Dependências
- Depende de `REQ-FUNC-003` (Demanda já delegada ao Fiscal) — que também está com gap em aberto.
- Depende de `REQ-DESEMP-001` (numeração sequencial atômica) — essa parte **funciona** e foi validada sob concorrência real.
- ~~Gap crítico e bloqueante: não existe `ContribuinteController` nem `ImovelController`...~~ **Corrigido em 13/09/2026.** Criados `ContribuinteService.buscarOuCriar()` e `ImovelService.buscarOuCriar()`: resolvem por `id` (se informado), depois por CPF/CNPJ (Contribuinte) ou inscrição (Imóvel), e só cadastram um registro novo se nenhum dos dois casos encontrar algo — evitando duplicatas e respeitando as constraints `UNIQUE` já existentes no banco. `AutoFiscalizacaoService.gerarAuto()` chama essa resolução antes de salvar o Auto. Não foram criados controllers REST dedicados para Contribuinte/Imóvel (CRUD completo) — não eram necessários para destravar este fluxo, e adicioná-los sem um pedido concreto seria escopo além do necessário.
- Erros de validação nessa resolução (ex.: nome do contribuinte ausente ao tentar cadastrar um novo) agora retornam `400` com `{"message": "..."}` — `FiscalController.emitirAuto()` passou a capturar `IllegalArgumentException` explicitamente, em vez de deixá-la estourar e ser mascarada como `403` pelo `/error` (mesmo padrão de correção já usado em `REQ-SEG-002`/`REQ-FUNC-003`).
- ~~Gap real: `FiscalController.emitirAuto()` não verifica posse da Demanda...~~ **Corrigido em 13/09/2026.** Adicionado o método privado `validarPosseDaDemanda()` em `FiscalController`, reutilizado por `emitirAuto()` e `emitirRelatorio()`: busca a Demanda no banco (não confia no objeto parcial enviado pelo cliente) e confirma que `demanda.fiscalAtribuido.id` bate com o `id` do usuário autenticado (resolvido via CPF do token JWT), retornando `403` com mensagem clara caso contrário.

### Notas e Suposições
- ~~Assume-se que uma mesma Demanda pode gerar, no máximo, um Auto de Fiscalização — não confirmado: nada no código impede múltiplos Autos para a mesma Demanda.~~ **Confirmado e reforçado em 22/09/2026** — `AutoFiscalizacaoService.gerarAuto()` agora rejeita explicitamente um segundo Auto para a mesma Demanda (ver TEST-007), em vez de depender apenas de o frontend não oferecer a opção.

### Anexos / Referências
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/FiscalController.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/service/AutoFiscalizacaoService.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/service/ContribuinteService.java` (novo)
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/service/ImovelService.java` (novo)
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/model/AutoFiscalizacao.java`

---

## Histórico de Alterações

| Versão | Data | Autor | Alteração |
|--------|------|-------|-----------|
| 1.0 | 10/09/2026 | Luiza | Criação inicial |
| 1.1 | 13/09/2026 | Luiza | Testado ao vivo pela primeira vez (nunca tinha sido executado com sucesso antes). Encontrado gap crítico e bloqueante: emissão de Auto pela tela real falha sempre, pois não há como criar Contribuinte/Imóvel via API. Isolando o teste com entidades pré-existentes, a numeração funciona corretamente. Encontrado também gap real: nenhuma checagem de que a Demanda pertence ao Fiscal que está emitindo. Status rebaixado para "Em revisão"; nenhuma correção aplicada ainda. |
| 1.2 | 13/09/2026 | Luiza | Gap crítico corrigido: criados `ContribuinteService`/`ImovelService` com resolução "busca por chave natural (CPF/CNPJ, inscrição) ou cria"; `AutoFiscalizacaoService` passou a usá-los antes de salvar. Testado com sucesso via API (dados novos e reaproveitamento) e pela **tela real** via Chrome headless (formulário preenchido e submetido de verdade) — TEST-001 a TEST-004 confirmados. Status permanece "Em revisão" só por causa do TEST-005 (checagem de posse da Demanda), que segue em aberto e não fazia parte do pedido desta correção. |
| 1.3 | 13/09/2026 | Luiza | Último gap corrigido: `FiscalController` agora valida, via `validarPosseDaDemanda()`, que a Demanda pertence ao Fiscal autenticado antes de emitir o Auto — `403` claro caso contrário. TEST-005/006 confirmados, sem regressão no fluxo legítimo. Status finalmente "Implementado" — todos os gaps conhecidos desta ficha foram corrigidos. |
| 1.4 | 22/09/2026 | Luiza | Verificação de atualidade: a suposição "não confirmada" de que uma Demanda gera no máximo um Auto agora está confirmada e reforçada no código (`existsByDemandaId` em `AutoFiscalizacaoService.gerarAuto()`, TEST-007). Também passou a existir edição/exclusão de Auto pelo criador (`PUT`/`DELETE /api/fiscal/autos/{id}`), fora do escopo original desta ficha — ver `REQ-SEG-001` v1.3 para a restrição de acesso de leitura correspondente. |
