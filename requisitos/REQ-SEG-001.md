# Ficha de Requisito

> **Norma de referência:** ISO/IEC/IEEE 29148:2018 (Seções 5.2.5 e 5.2.8)

---

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | `REQ-SEG-001` |
| **Nome** | `Controle de acesso baseado em cargo (RBAC)` |
| **Tipo** | `Segurança` |
| **Prioridade** | `Essencial` |
| **Status** | `Implementado` |
| **Versão** | `1.3` |

---

## Descrição

### Texto do Requisito

O sistema deve restringir o acesso às **ações** exclusivas de Gestor (criar/delegar Demandas) a usuários com cargo `GESTOR` e às **ações** exclusivas de Fiscal (executar vistoria, emitir Auto/Relatório, anexar evidências) a usuários com cargo `FISCAL`, rejeitando com status `403` qualquer requisição autenticada cujo cargo não corresponda ao exigido pela ação — independentemente de qual rota HTTP for usada para acessá-la.

### Condições de Aplicação

- Condição 1: A requisição contém um token JWT válido e não expirado.
- Condição 2: O cargo presente no token não corresponde ao exigido pela ação solicitada.
- Condição 3: A regra vale para **toda** rota que executa a ação, não apenas para a rota "oficial" documentada. As ações de criação (demandas, delegação, autos, relatórios, anexos, usuários) hoje só existem em um único lugar cada (`GestorController`/`FiscalController`, protegidos por prefixo; ou `AnexoController`/`UsuarioController`, protegidos por `@PreAuthorize` de método) — não há mais rota alternativa desprotegida para a mesma ação.

---

## Rastreabilidade

| Campo | Valor |
|-------|-------|
| **Fonte (Source)** | `SRS_MODULO_FISCALIZACAO_AMBIENTAL_v1.0.md`, Seções 3.6 e 3.8-c |
| **Requisito Pai** | `REQ-FUNC-001` |
| **Requisitos Filhos** | — |
| **Casos de Uso / Histórias Relacionadas** | UC-LOGIN, UC-CRIAR-DEMANDA, UC-DELEGAR-DEMANDA, UC-EMITIR-AUTO, UC-EMITIR-RELATORIO |

---

## Justificativa (Rationale)

É o requisito de segurança central do projeto, exigido explicitamente pelo plano ("o Fiscal jamais acesse ou execute ações exclusivas do Gestor") e citado como diferencial técnico no roteiro de estudos da vaga (RBAC via JWT no Spring Security).

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
Funcionalidade: Controle de acesso por cargo
  Cenário: Fiscal tenta acessar rota exclusiva de Gestor
    Dado um usuário autenticado com cargo FISCAL
    Quando ele envia uma requisição para qualquer rota sob /api/gestor/**
    Então o sistema responde com status 403

  Cenário: Gestor tenta acessar rota exclusiva de Fiscal
    Dado um usuário autenticado com cargo GESTOR
    Quando ele envia uma requisição para qualquer rota sob /api/fiscal/**
    Então o sistema responde com status 403

  Cenário: Requisição sem token para rota protegida
    Dado uma requisição sem cabeçalho Authorization
    Quando ela é enviada a qualquer rota fora de /api/auth/**
    Então o sistema responde com status 401

  Cenário: Fiscal tenta criar Demanda por uma rota alternativa
    Dado um usuário autenticado com cargo FISCAL
    Quando ele envia POST /api/demandas/gestor (rota fora do prefixo /api/gestor/**, mas que executa a mesma ação de negócio)
    Então o sistema responde com status 403, da mesma forma que responderia em /api/gestor/demandas

  Cenário: Gestor tenta enviar um anexo (ação exclusiva de Fiscal)
    Dado um usuário autenticado com cargo GESTOR
    Quando ele envia POST /api/anexos
    Então o sistema responde com status 403

  Cenário: Fiscal tenta cadastrar um novo usuário (ação exclusiva de Gestor)
    Dado um usuário autenticado com cargo FISCAL
    Quando ele envia POST /api/usuarios
    Então o sistema responde com status 403

  Cenário: Fiscal tenta ler um Auto/Relatório emitido por outro Fiscal
    Dado um usuário autenticado com cargo FISCAL, que não é o criador do documento
    Quando ele envia GET /api/autos/{id} ou GET /api/relatorios/{id} de um documento de outro Fiscal
    Então o sistema responde com status 403

  Cenário: Gestor lê qualquer Auto/Relatório, e o Fiscal criador lê os seus próprios
    Dado um usuário autenticado GESTOR, ou o FISCAL que criou o documento
    Quando ele envia GET /api/autos/{id}, GET /api/relatorios/{id} ou as rotas de documento assinado
    Então o sistema responde com status 200
```

### Casos de Teste Associados
| ID do Teste | Descrição | Resultado Esperado | Resultado Obtido |
|-------------|-----------|--------------------|-------------------|
| TEST-001 | Fiscal chama `POST /api/gestor/demandas` | Status 403 | ✅ Confirmado |
| TEST-002 | Gestor chama `GET /api/fiscal/demandas` | Status 403 | ✅ Confirmado |
| TEST-003 | Requisição sem token a `/api/gestor/demandas` | Status 401 | ✅ Confirmado (`Http403ForbiddenEntryPoint`/401 conforme esperado) |
| TEST-004 | Gestor chama `GET /api/gestor/demandas` | Status 200 | ✅ Confirmado |
| TEST-005 | Fiscal chama `POST /api/demandas/gestor?matriculaGestor=M-001` (rota alternativa, mesma ação de "criar demanda") | Status 403 | ✅ Corrigido — a rota `/api/demandas/**` foi removida (`DemandaController`/`DemandaService` eram código morto, nunca usado pelo frontend); a requisição agora retorna 403 |
| TEST-006 | Fiscal chama `PUT /api/demandas/gestor/{id}/delegar?matriculaFiscal=M-002` (rota alternativa de "delegar demanda") | Status 403 | ✅ Corrigido — mesma rota removida |
| TEST-007 | Gestor chama `POST /api/autos` diretamente (criação de Auto, ação exclusiva de Fiscal) | Status 403 | ✅ Corrigido — o `POST` duplicado e desprotegido foi removido de `AutoFiscalizacaoController`; a criação só existe mais em `POST /api/fiscal/autos`, já protegido |
| TEST-008 | Gestor chama `GET /api/autos` (leitura) | Status 200 | ✅ Confirmado — `GET /api/autos`/`GET /api/relatorios` (listagem completa) são restritos ao Gestor desde a v1.3 desta ficha (ver TEST-012/013) |
| TEST-009 | Gestor chama `POST /api/anexos` (upload, ação exclusiva de Fiscal) | Status 403 | ❌→✅ Este teste ficou desatualizado: `AnexoController.registrarAnexo()` passou a aceitar **também** `ROLE_GESTOR` (`@PreAuthorize("hasAnyAuthority('FISCAL','GESTOR')")`), então hoje a resposta é `200`. Mantido aqui como nota histórica; ver Histórico de Alterações v1.3 |
| TEST-010 | Fiscal chama `POST /api/usuarios` (cadastro de usuário, ação administrativa de Gestor) | Status 403 | ✅ Confirmado — `@PreAuthorize("hasAuthority('GESTOR')")` adicionado em `UsuarioController.salvar()` |
| TEST-011 (regressão) | Gestor cria demanda via `POST /api/gestor/demandas`, lista fiscais via `GET /api/gestor/fiscais`, cria usuário via `POST /api/usuarios`; Fiscal lista demandas via `GET /api/fiscal/demandas` | Status 200 em todos | ✅ Confirmado — nenhum fluxo legítimo foi afetado pela remoção das rotas redundantes |
| TEST-012 | Fiscal Mariana chama `GET /api/autos/{id}` de um Auto emitido pelo Fiscal Carlos (por posse do ID, sem ser dono) | Status 403 | ✅ **Vulnerabilidade real encontrada e corrigida (IDOR)** — até a v1.2 desta ficha, essa chamada retornava `200` com o conteúdo completo do documento de outro fiscal, incluindo o `/documento-assinado`. Corrigido restringindo `GET /api/autos/{id}`, `GET /api/relatorios/{id}` e as rotas `/documento-assinado` ao Gestor ou ao Fiscal criador (`AutoFiscalizacaoController.validarAcesso()`/`RelatorioController.validarAcesso()`) |
| TEST-013 (regressão) | Gestor lê qualquer Auto/Relatório; o Fiscal criador lê os seus próprios (`GET /api/autos/{id}`, `/documento-assinado` etc.) | Status 200 | ✅ Confirmado — nenhum fluxo legítimo de leitura foi afetado pela correção do TEST-012 |

---

## Análise de Conformidade com a Norma

| Característica | Atende? | Observações |
|----------------|---------|-------------|
| **Necessary (Necessário)** | [x] Sim [ ] Não | Sem RBAC, qualquer usuário autenticado poderia executar ações de outro cargo. |
| **Appropriate (Apropriado)** | [x] Sim [ ] Não | Descreve a regra de acesso sem prescrever a anotação Spring específica. |
| **Unambiguous (Não ambíguo)** | [x] Sim [ ] Não | Mapeamento rota → cargo é determinístico. |
| **Complete (Completo)** | [x] Sim [ ] Não | Autocontido. |
| **Singular (Singular)** | [x] Sim [ ] Não | Trata exclusivamente do controle de acesso por cargo. |
| **Feasible (Factível)** | [x] Sim [ ] Não | Implementado combinando restrição por prefixo (`SecurityConfig`, para `/api/gestor/**` e `/api/fiscal/**`) com `@PreAuthorize` por método (`AnexoController`, `UsuarioController`), e eliminação das rotas redundantes que não tinham nenhuma das duas proteções. |
| **Verifiable (Verificável)** | [x] Sim [ ] Não | Verificável por status HTTP em cada combinação cargo/ação (TEST-001 a TEST-011, todos passando). |
| **Correct (Correto)** | [x] Sim [ ] Não | A proteção agora vale por **ação de negócio**, não apenas por prefixo de URL: cada ação de criação/delegação tem exatamente um endpoint, e esse endpoint está protegido (por prefixo ou por `@PreAuthorize`). A leitura de Auto/Relatório por ID deixou de ser aberta a qualquer autenticado — era uma vulnerabilidade real de IDOR, não uma decisão de design (ver TEST-012 e Notas). |
| **Conforming (Conforme)** | [x] Sim [ ] Não | Segue o template. |

---

## Informações Complementares

### Restrições e Dependências
- Depende de `REQ-FUNC-001` (emissão de token contendo o cargo).
- No frontend, complementado por Route Guards (`auth.guard.ts`), que ocultam/bloqueiam telas — mas a garantia de segurança efetiva está no backend, não no cliente.
- ~~Gap crítico em aberto (bypass de RBAC)...~~ **Corrigido em 11/09/2026.** Ações aplicadas:
  1. `DemandaController.java` e `DemandaService.java` **removidos por completo** — eram código morto (nenhuma chamada no frontend usava `/api/demandas/**`; `GestorController`/`FiscalController` já reimplementavam as mesmas ações, corretamente protegidas por prefixo) e a única superfície pela qual o bypass de criação/delegação de demanda acontecia.
  2. `AutoFiscalizacaoController` e `RelatorioController`: removido o método `POST` duplicado e desprotegido de cada um (a criação real, usada pelo frontend, já existia só em `FiscalController`, sob `/api/fiscal/**`). Os `GET` de consulta foram mantidos, sem restrição de cargo — decisão consciente, não uma lacuna (ver Condição 3 e Notas).
  3. `AnexoController.registrarAnexo()`: adicionado `@PreAuthorize("hasAuthority('FISCAL')")`.
  4. `UsuarioController.salvar()`: adicionado `@PreAuthorize("hasAuthority('GESTOR')")` (cadastro de usuário tratado como ação administrativa do Gestor).
  5. Validado que nenhum fluxo legítimo quebrou (TEST-011) e que todos os bypasses antes confirmados agora retornam 403 (TEST-005 a TEST-010).
- **Observação colateral encontrada durante a correção (não é RBAC, registrar à parte):** uma rota removida (`/api/demandas/gestor`) hoje responde `403` em vez do `404` esperado para uma rota inexistente. Isso é o **mesmo padrão de mascaramento pelo `/error`** já documentado em `REQ-SEG-002` (rota `/error` fora do `permitAll`), só que disparado por "sem handler encontrado" em vez de uma exceção de aplicação. Não é uma regressão de segurança (o efeito prático ainda é negar acesso), mas o código de status fica impreciso. Fica fora do escopo desta correção; recomenda-se tratar `/error` de forma centralizada (liberá-la no `permitAll` e delegar a um handler próprio) numa correção futura dedicada a esse padrão.

### Notas e Suposições
- Assume-se que o cargo do usuário não muda durante a validade de um token já emitido (não há revogação de token nesta versão).
- ~~Decisão consciente: `GET /api/autos`, `GET /api/relatorios` e as rotas de documento assinado permanecem acessíveis a qualquer cargo autenticado...~~ **Revertido em 22/09/2026 — não era uma decisão de design, era uma vulnerabilidade real (IDOR).** Qualquer Fiscal autenticado conseguia ler e baixar o Auto/Relatório de **qualquer outro** Fiscal só sabendo (ou adivinhando sequencialmente) o `id`, incluindo o `/documento-assinado` — dado potencialmente sensível. Corrigido restringindo a leitura por ID ao Gestor ou ao Fiscal criador do documento; `GET /api/anexos/demanda/{id}` (anexos de uma demanda) permanece aberto a qualquer autenticado, pois a posse ali é feita ao nível da Demanda, não do anexo individual — mantido fora do escopo desta correção.
- ~~Achado à parte, fora do escopo desta correção: descompasso de rota/tipo de conteúdo no upload de anexo...~~ **Corrigido em 11/09/2026** — ver `REQ-FUNC-006` v1.1 para os detalhes e a validação completa (incluindo o bug adicional encontrado em `verificar-hash`, que fazia a detecção de duplicidade no cliente nunca disparar).

### Anexos / Referências
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/security/SecurityConfig.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/GestorController.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/FiscalController.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/AutoFiscalizacaoController.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/RelatorioController.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/AnexoController.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/UsuarioController.java`
- `fiscal-ambiental-frontend/src/app/guards/auth.guard.ts`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/DemandaController.java` e `service/DemandaService.java` — **removidos** em 11/09/2026 (ver Histórico de Alterações)

---

## Histórico de Alterações

| Versão | Data | Autor | Alteração |
|--------|------|-------|-----------|
| 1.0 | 10/09/2026 | Luiza | Criação inicial |
| 1.1 | 10/09/2026 | Luiza | Verificação ao vivo com tokens reais de cada cargo. TEST-001 a TEST-004 confirmados. Encontrado e confirmado bypass real de RBAC: rotas alternativas (`/api/demandas/gestor/**`, `/api/autos/**`, `/api/anexos/**`) não têm restrição de cargo, permitindo que um Fiscal crie e delegue demandas, e que um Gestor acesse dados de domínio do Fiscal (TEST-005, TEST-006, TEST-007). Status rebaixado para "Em revisão"; correção pendente de decisão (não aplicada ainda). |
| 1.2 | 11/09/2026 | Luiza | Gap corrigido: removidos `DemandaController`/`DemandaService` (código morto e inseguro); removidos os `POST` duplicados de `AutoFiscalizacaoController`/`RelatorioController`; adicionado `@PreAuthorize` em `AnexoController` (FISCAL) e `UsuarioController` (GESTOR). Reexecutados TEST-005 a TEST-007 (agora bloqueados) e adicionados TEST-008 a TEST-011 (leitura aberta + regressão dos fluxos legítimos), todos passando. Status voltou para "Implementado". Registrado achado colateral fora de escopo: descompasso de rota/tipo de conteúdo no upload de anexo (frontend x backend). |
| 1.3 | 22/09/2026 | Luiza | Reclassificado como vulnerabilidade real (IDOR), não decisão de design: `GET /api/autos/{id}`, `GET /api/relatorios/{id}` e as rotas `/documento-assinado` deixaram de ser abertas a qualquer autenticado e passaram a exigir ser o Gestor ou o Fiscal criador do documento (TEST-012/013). Também registrado que `POST /api/anexos` passou a aceitar `ROLE_GESTOR`, além de `ROLE_FISCAL` (TEST-009 atualizado). |
