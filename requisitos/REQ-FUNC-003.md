# Ficha de Requisito

> **Norma de referência:** ISO/IEC/IEEE 29148:2018 (Seções 5.2.5 e 5.2.8)

---

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | `REQ-FUNC-003` |
| **Nome** | `Delegação de Demanda a um Fiscal` |
| **Tipo** | `Funcional` |
| **Prioridade** | `Essencial` |
| **Status** | `Implementado` |
| **Versão** | `1.3` |

---

## Descrição

### Texto do Requisito

O sistema deve permitir que um Gestor atribua uma Demanda existente a um usuário com cargo Fiscal ativo, alterando o status da Demanda para `EM_ANDAMENTO`.

### Condições de Aplicação

- Condição 1: A Demanda referenciada existe e está com status `PENDENTE`.
- Condição 2: O usuário destinatário possui cargo `FISCAL` e está ativo.
- Condição 3: A requisição é enviada por um usuário autenticado com cargo `GESTOR`.

---

## Rastreabilidade

| Campo | Valor |
|-------|-------|
| **Fonte (Source)** | `SRS_MODULO_FISCALIZACAO_AMBIENTAL_v1.0.md`, Seções 2.2 e 3.2 |
| **Requisito Pai** | `REQ-FUNC-002` |
| **Requisitos Filhos** | `REQ-FUNC-004`, `REQ-FUNC-005` |
| **Casos de Uso / Histórias Relacionadas** | UC-DELEGAR-DEMANDA |

---

## Justificativa (Rationale)

Sem a delegação explícita, o Fiscal não teria como identificar quais demandas lhe foram atribuídas, quebrando o modelo de fila restrita por usuário descrito no plano do projeto.

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
Funcionalidade: Delegação de Demanda
  Cenário: Gestor delega demanda a um fiscal ativo
    Dado uma demanda com status PENDENTE
    E um usuário com cargo FISCAL e ativo igual a verdadeiro
    Quando o Gestor envia PUT /api/gestor/demandas/{demandaId}/delegar/{fiscalId}
    Então a demanda passa a ter status EM_ANDAMENTO e fiscalAtribuido preenchido

  Cenário: Gestor tenta delegar a um usuário que não é fiscal
    Dado um usuário com cargo GESTOR como destinatário
    Quando o Gestor tenta delegar a demanda a esse usuário
    Então o sistema rejeita a operação
```

### Casos de Teste Associados
| ID do Teste | Descrição | Resultado Esperado | Resultado Obtido |
|-------------|-----------|--------------------|-------------------|
| TEST-001 | Delegar demanda pendente a fiscal ativo | Status 200, demanda `EM_ANDAMENTO` | ✅ Confirmado |
| TEST-002 | Delegar a usuário com cargo GESTOR | Erro de validação | ✅ Corrigido — responde `400` com `"Só é possível delegar demandas a usuários com cargo FISCAL."`; nada é persistido |
| TEST-003 | Delegar a fiscal inativo (`ativo = false`) | Erro de validação | ✅ Corrigido — responde `400` com `"O fiscal informado está inativo."` |
| TEST-004 (regressão) | Delegar demanda pendente a fiscal ativo, após a correção | Status 200, demanda `EM_ANDAMENTO` | ✅ Confirmado — a correção não quebrou o fluxo legítimo |
| TEST-005 | Tentar redelegar uma demanda já `EM_ANDAMENTO` para outro fiscal | Erro de validação, `fiscalAtribuido` original preservado | ✅ Corrigido — responde `400` com `"Só é possível delegar demandas com status PENDENTE. Esta demanda já está EM_ANDAMENTO."`; conferido que o fiscal originalmente atribuído continua o mesmo |

---

## Análise de Conformidade com a Norma

| Característica | Atende? | Observações |
|----------------|---------|-------------|
| **Necessary (Necessário)** | [x] Sim [ ] Não | Etapa 1 do fluxo principal (atribuição). |
| **Appropriate (Apropriado)** | [x] Sim [ ] Não | Não especifica algoritmo de seleção de fiscal, apenas a regra de validação. |
| **Unambiguous (Não ambíguo)** | [x] Sim [ ] Não | Única interpretação do efeito colateral (mudança de status). |
| **Complete (Completo)** | [x] Sim [ ] Não | Autocontido. |
| **Singular (Singular)** | [x] Sim [ ] Não | Trata apenas da delegação. |
| **Feasible (Factível)** | [x] Sim [ ] Não | Já implementado (`GestorController`, `DemandaService`). |
| **Verifiable (Verificável)** | [x] Sim [ ] Não | Verificável pelo estado persistido da demanda e pela resposta HTTP (TEST-001 a TEST-005). |
| **Correct (Correto)** | [x] Sim [ ] Não | `GestorController.delegarDemanda()` agora verifica cargo `FISCAL` e `ativo == true` antes de atribuir, retornando `400` com mensagem clara quando a condição não é atendida. |
| **Conforming (Conforme)** | [x] Sim [ ] Não | Segue o template. |

---

## Informações Complementares

### Restrições e Dependências
- Depende de `REQ-FUNC-002` (existência prévia da Demanda).
- Depende de `REQ-SEG-001` (RBAC) para restringir a operação a Gestores.
- ~~Gap real em aberto: `GestorController.delegarDemanda()` não validava cargo nem status ativo...~~ **Corrigido em 13/09/2026.** O método agora verifica `"FISCAL".equalsIgnoreCase(fiscal.getCargo())` e `Boolean.FALSE.equals(fiscal.getAtivo())` antes de atribuir, retornando `400 Bad Request` com mensagem específica para cada caso — validação explícita no controller, mesmo padrão de resposta direta usado em `AnexoController`, sem depender do `GlobalExceptionHandler` (não é um erro de bean validation, é uma regra de negócio que depende de dados já buscados do banco).
- ~~Ainda não validado: redelegar uma demanda já EM_ANDAMENTO/CONCLUIDO...~~ **Corrigido em 13/09/2026.** Adicionada checagem `!"PENDENTE".equals(demanda.getStatus())` no início do método, antes de qualquer outra validação — retorna `400` com o status atual da demanda na mensagem. Isso também fecha, de quebra, a Condição 1 desta própria ficha ("a Demanda... está com status PENDENTE"), que já existia no texto do requisito desde a v1.0 mas nunca tinha sido implementada.
- ~~`Fiscal-Ambiental/.../service/DemandaService.java`~~ — **removido em 11/09/2026** (`REQ-SEG-001` v1.2): era código morto, nunca usado pelo frontend, e expunha a mesma ação sem RBAC por uma rota alternativa. Toda a lógica de delegação está hoje só em `GestorController`.

### Notas e Suposições
- Assume-se que uma Demanda só pode estar atribuída a um único Fiscal por vez (não há corregência de responsabilidade na tabela `demandas`).

### Anexos / Referências
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/GestorController.java`

---

## Histórico de Alterações

| Versão | Data | Autor | Alteração |
|--------|------|-------|-----------|
| 1.0 | 10/09/2026 | Luiza | Criação inicial |
| 1.1 | 13/09/2026 | Luiza | TEST-001 confirmado. Encontrado gap real (TEST-002): delegação não valida cargo nem status ativo do destinatário — testado ao vivo delegando para um Gestor, aceito sem erro. Status rebaixado para "Em revisão"; correção pendente. Referência a `DemandaService` removida (arquivo excluído em `REQ-SEG-001` v1.2). |
| 1.2 | 13/09/2026 | Luiza | Gap corrigido: `GestorController.delegarDemanda()` agora valida cargo `FISCAL` e `ativo == true`, retornando `400` com mensagem clara caso contrário. TEST-002/003 revalidados (agora passam) e TEST-004 adicionado (regressão do fluxo legítimo). Status voltou para "Implementado". Registrado gap secundário não corrigido: é possível redelegar uma demanda já `EM_ANDAMENTO` para outro fiscal sem aviso. |
| 1.3 | 13/09/2026 | Luiza | Gap secundário corrigido: adicionada checagem de que a Demanda precisa estar `PENDENTE` para ser delegada — redelegação de demanda `EM_ANDAMENTO` agora retorna `400` e preserva o fiscal originalmente atribuído (TEST-005). Isso também passou a cumprir a Condição 1 do requisito, presente desde a v1.0 mas nunca antes implementada. |
