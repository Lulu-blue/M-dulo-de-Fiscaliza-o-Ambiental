# Ficha de Requisito

> **Norma de referência:** ISO/IEC/IEEE 29148:2018 (Seções 5.2.5 e 5.2.8)

---

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | `REQ-FUNC-002` |
| **Nome** | `Criação de Demanda de Fiscalização pelo Gestor` |
| **Tipo** | `Funcional` |
| **Prioridade** | `Essencial` |
| **Status** | `Implementado` |
| **Versão** | `1.1` |

---

## Descrição

### Texto do Requisito

O sistema deve permitir que um usuário com cargo Gestor registre uma nova Demanda de Fiscalização, contendo título e descrição, com status inicial `PENDENTE`.

### Condições de Aplicação

- Condição 1: A requisição é enviada autenticada, com token JWT de um usuário cujo cargo seja `GESTOR`.
- Condição 2: A requisição é enviada a `POST /api/gestor/demandas`. ~~(ou `POST /api/demandas/gestor`)~~ — essa rota alternativa existia na v1.0 desta ficha, mas foi **removida em 11/09/2026** por ser um bypass de RBAC não intencional (ver `REQ-SEG-001` v1.2); hoje `POST /api/gestor/demandas` é o único caminho.

---

## Rastreabilidade

| Campo | Valor |
|-------|-------|
| **Fonte (Source)** | `SRS_MODULO_FISCALIZACAO_AMBIENTAL_v1.0.md`, Seções 2.2, 3.1 e 3.2 |
| **Requisito Pai** | — |
| **Requisitos Filhos** | `REQ-FUNC-003` |
| **Casos de Uso / Histórias Relacionadas** | UC-CRIAR-DEMANDA |

---

## Justificativa (Rationale)

É o ponto de partida do fluxo principal da aplicação: sem a criação da Demanda pelo Gestor, não há trabalho a ser atribuído a um Fiscal.

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
Funcionalidade: Criação de Demanda
  Cenário: Gestor cria uma nova demanda
    Dado um usuário autenticado com cargo GESTOR
    Quando ele envia POST /api/gestor/demandas com título e descrição
    Então o sistema cria a demanda com status PENDENTE e registra a data de criação

  Cenário: Fiscal tenta criar uma demanda
    Dado um usuário autenticado com cargo FISCAL
    Quando ele envia POST /api/gestor/demandas
    Então o sistema responde com status 403
```

### Casos de Teste Associados
| ID do Teste | Descrição | Resultado Esperado | Resultado Obtido |
|-------------|-----------|--------------------|-------------------|
| TEST-001 | Gestor cria demanda com dados válidos | Status 200/201, demanda com status `PENDENTE` | ✅ Confirmado repetidas vezes ao longo da sessão (status real retornado é `200`, não `201` — `GestorController` usa `ResponseEntity.ok()`, não `.created()`) |
| TEST-002 | Fiscal tenta criar demanda | Status 403 | ✅ Confirmado |

---

## Análise de Conformidade com a Norma

| Característica | Atende? | Observações |
|----------------|---------|-------------|
| **Necessary (Necessário)** | [x] Sim [ ] Não | Etapa 1 do fluxo principal descrito no plano do projeto. |
| **Appropriate (Apropriado)** | [x] Sim [ ] Não | Não define layout de tela, apenas o comportamento da API. |
| **Unambiguous (Não ambíguo)** | [x] Sim [ ] Não | Ator, ação e resultado únicos. |
| **Complete (Completo)** | [x] Sim [ ] Não | Autocontido. |
| **Singular (Singular)** | [x] Sim [ ] Não | Trata apenas da criação, não da delegação. |
| **Feasible (Factível)** | [x] Sim [ ] Não | Já implementado (`GestorController`, `DemandaService`). |
| **Verifiable (Verificável)** | [x] Sim [ ] Não | Verificável por status HTTP e estado persistido. |
| **Correct (Correto)** | [x] Sim [ ] Não | Reflete `Demanda.java` (status inicial, `dataCriacao`). |
| **Conforming (Conforming)** | [x] Sim [ ] Não | Segue o template. |

---

## Informações Complementares

### Restrições e Dependências
- Depende de `REQ-SEG-001` (RBAC) para restringir a criação apenas a Gestores.

### Notas e Suposições
- Assume-se que o campo `descrição` é opcional na criação, podendo ser complementado posteriormente — **confirmado**: `GestorController.criarDemanda()` não valida nenhum campo (nem `@Valid`, nem checagem manual); título e descrição vazios são aceitos silenciosamente. Não é tratado como bug porque não há requisito explícito de obrigatoriedade, mas fica registrado como comportamento permissivo não intencional.

### Anexos / Referências
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/GestorController.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/model/Demanda.java`

---

## Histórico de Alterações

| Versão | Data | Autor | Alteração |
|--------|------|-------|-----------|
| 1.0 | 10/09/2026 | Luiza | Criação inicial |
| 1.1 | 13/09/2026 | Luiza | Verificação completa: TEST-001/002 confirmados ao vivo. Corrigida referência à rota `/api/demandas/gestor`, removida por segurança em `REQ-SEG-001` v1.2. Registrado que a criação não valida campos obrigatórios (comportamento permissivo, não é bug por não haver requisito de obrigatoriedade). |
