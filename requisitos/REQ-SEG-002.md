# Ficha de Requisito

> **Norma de referência:** ISO/IEC/IEEE 29148:2018 (Seções 5.2.5 e 5.2.8)

---

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | `REQ-SEG-002` |
| **Nome** | `Armazenamento seguro de senha do usuário` |
| **Tipo** | `Segurança` |
| **Prioridade** | `Essencial` |
| **Status** | `Implementado` |
| **Versão** | `1.2` |

---

## Descrição

### Texto do Requisito

O sistema deve armazenar a senha de cada usuário exclusivamente na forma de hash criptográfico (BCrypt) na coluna `senha_hash`, sem persistir ou retornar a senha em texto plano em nenhuma resposta da API.

### Condições de Aplicação

- Condição 1: Aplica-se a toda operação de criação de usuário (`POST /api/usuarios`). Não existe, nesta versão, um endpoint dedicado de atualização (`PUT`/`PATCH`) — o `UsuarioController` expõe apenas listagem, consulta por ID/matrícula e criação. A menção original a "atualização" nesta condição não corresponde a nenhuma rota implementada.
- Condição 2: Aplica-se a toda resposta de API que inclua dados de `usuarios`, incluindo referências aninhadas (ex.: `gestorCriador`/`fiscalAtribuido` dentro de `Demanda`).
- Condição 3: A criação exige `nome`, `email` (formato válido), `senhaHash` (senha em texto plano no payload de entrada) e `cargo` preenchidos; requisições incompletas são rejeitadas com `400 Bad Request` antes de qualquer tentativa de hashing/persistência.

---

## Rastreabilidade

| Campo | Valor |
|-------|-------|
| **Fonte (Source)** | `SRS_MODULO_FISCALIZACAO_AMBIENTAL_v1.0.md`, Seções 3.5 e 3.8-c2 |
| **Requisito Pai** | `REQ-FUNC-001` |
| **Requisitos Filhos** | — |
| **Casos de Uso / Histórias Relacionadas** | UC-LOGIN |

---

## Justificativa (Rationale)

Armazenar senha em texto plano é uma falha de segurança básica; o uso de BCrypt (via `PasswordEncoder` do Spring Security) é o padrão mínimo esperado em qualquer avaliação técnica corporativa, conforme reforçado no roteiro de estudos da vaga.

---

## Critérios de Verificação e Aceitação

### Método de Verificação
- [x] Inspeção / Revisão
- [x] Teste
- [ ] Demonstração
- [ ] Análise
- [ ] Simulação

### Critérios de Aceitação (Gherkin / BDD recomendado)
```gherkin
Funcionalidade: Armazenamento seguro de senha
  Cenário: Cadastro de novo usuário
    Dado uma senha em texto plano informada na criação de um usuário
    Quando o usuário é persistido
    Então a coluna senha_hash contém um hash BCrypt, nunca a senha original

  Cenário: Consulta de usuário via API
    Dado um usuário já cadastrado
    Quando qualquer endpoint retorna os dados desse usuário
    Então o campo de senha (hash ou texto plano) não está presente na resposta

  Cenário: Cadastro sem informar senha
    Dado um payload de criação de usuário sem o campo senhaHash
    Quando ele é enviado para POST /api/usuarios
    Então o sistema responde 400 Bad Request com uma mensagem indicando o campo faltante
    E nenhum usuário é persistido no banco
```

### Casos de Teste Associados
| ID do Teste | Descrição | Resultado Esperado | Resultado Obtido |
|-------------|-----------|--------------------|-------------------|
| TEST-001 | `POST /api/usuarios` com senha em texto plano, depois inspecionar `senha_hash` | Valor inicia com prefixo BCrypt (`$2a$`/`$2b$`) | ✅ Confirmado (`$2a$10$...`), e login subsequente com a senha original funciona |
| TEST-002 | Chamar `GET /api/usuarios/{id}` | Resposta não contém `senhaHash` nem senha em texto plano | ✅ Confirmado |
| TEST-003 | Chamar `GET /api/gestor/demandas` (retorna `Usuario` aninhado em `gestorCriador`) | Objeto aninhado também não expõe `senhaHash` | ✅ Confirmado — a anotação é de classe (`@JsonProperty` em `Usuario`), vale para toda serialização, não só nos endpoints de usuário |
| TEST-004 | `POST /api/usuarios` **sem** o campo `senhaHash` no corpo | Deveria responder `400 Bad Request` com mensagem clara | ✅ Corrigido — responde `400` com `{"mensagem":"Dados inválidos.","erros":{"senhaHash":"Senha é obrigatória"}}`; nenhum registro é criado |
| TEST-005 | `POST /api/usuarios` com corpo `{}` (todos os campos obrigatórios ausentes) | `400 Bad Request` listando todos os campos faltantes | ✅ Confirmado — retorna `nome`, `email`, `senhaHash` e `cargo` juntos na mesma resposta |
| TEST-006 | `POST /api/usuarios` com todos os campos obrigatórios válidos (regressão) | `200 OK`, usuário criado com senha em hash | ✅ Confirmado — segue funcionando normalmente após a adição da validação |

---

## Análise de Conformidade com a Norma

| Característica | Atende? | Observações |
|----------------|---------|-------------|
| **Necessary (Necessário)** | [x] Sim [ ] Não | Requisito mínimo de proteção de credenciais. |
| **Appropriate (Apropriado)** | [x] Sim [ ] Não | Especifica o algoritmo (BCrypt) por já ser a escolha implementada e citada como padrão de mercado. |
| **Unambiguous (Não ambíguo)** | [x] Sim [ ] Não | Regra objetiva sobre o que nunca deve ocorrer. |
| **Complete (Completo)** | [x] Sim [ ] Não | Autocontido. |
| **Singular (Singular)** | [x] Sim [ ] Não | Trata exclusivamente do armazenamento/exposição de senha. |
| **Feasible (Factível)** | [x] Sim [ ] Não | Implementado (`BCryptPasswordEncoder` em `SecurityConfig`, aplicado em `UsuarioService.salvar()`). |
| **Verifiable (Verificável)** | [x] Sim [ ] Não | Verificado por inspeção de banco e de payload de resposta (TEST-001 a TEST-006, todos passando). |
| **Correct (Correto)** | [x] Sim [ ] Não | Com a Condição 3 adicionada (validação de campos obrigatórios), o requisito agora cobre também o comportamento esperado para entrada incompleta, e a implementação corresponde a ele. |
| **Conforming (Conforme)** | [x] Sim [ ] Não | Segue o template. |

---

## Informações Complementares

### Restrições e Dependências
- ~~Depende da anotação `@JsonIgnore`...~~ **Resolvido em 10/09/2026.** `Usuario.senhaHash` usa `@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)` — aceita a senha na entrada (criação) e nunca a serializa na saída, em nenhum endpoint que retorne `Usuario` direta ou aninhadamente. `UsuarioService.salvar()` também foi corrigido para aplicar `PasswordEncoder.encode()` antes de persistir (antes salvava em texto plano).
- ~~Gap em aberto: `UsuarioService.salvar()` não valida se `senhaHash` foi informado...~~ **Resolvido em 10/09/2026.** Adicionadas anotações Bean Validation (`@NotBlank` em `nome`, `email`, `senhaHash`, `cargo`; `@Email` em `email`) em `Usuario.java`, `@Valid` em `UsuarioController.salvar()`, e um `GlobalExceptionHandler` (`@RestControllerAdvice`) que intercepta `MethodArgumentNotValidException` e responde `400` com a lista de campos inválidos — a exceção nunca mais chega a vazar para `/error`.
- **Risco não testado:** como não há endpoint de atualização dedicado, se `POST /api/usuarios` for reaproveitado enviando um `id` existente (o `JpaRepository.save()` faz upsert por chave primária), `salvar()` recriptografaria o que estiver em `senhaHash` no payload — se o cliente reenviar um hash já criptografado (por não ter a senha original em mãos), o resultado seria um hash-do-hash, invalidando o login do usuário. Não foi reproduzido nem confirmado; registrado como hipótese a verificar antes de expor uma rota de atualização.

### Notas e Suposições
- Assume-se que o algoritmo BCrypt com fator de custo padrão do Spring Security é suficiente para o contexto de portfólio (não há exigência de rotação de senha ou política de complexidade nesta versão).
- A senha mínima/máxima e regras de complexidade não são validadas em nenhuma camada (frontend ou backend) — aceito conscientemente como fora de escopo para este portfólio.

### Anexos / Referências
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/security/SecurityConfig.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/model/Usuario.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/UsuarioController.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/exception/GlobalExceptionHandler.java`
- `Fiscal-Ambiental/src/main/resources/db/migration/V5__seed_usuarios_iniciais.sql`

---

## Histórico de Alterações

| Versão | Data | Autor | Alteração |
|--------|------|-------|-----------|
| 1.0 | 10/09/2026 | Luiza | Criação inicial |
| 1.1 | 10/09/2026 | Luiza | Verificação pós-implementação: confirmado hashing (TEST-001) e não exposição direta/aninhada (TEST-002, TEST-003). Encontrado e documentado gap real: `POST /api/usuarios` sem senha derruba com exceção não tratada, mascarada como 403 (TEST-004). Status rebaixado para "Em revisão" até a validação de entrada ser implementada. |
| 1.2 | 10/09/2026 | Luiza | Gap corrigido: validação Bean Validation nos campos obrigatórios de `Usuario` + `GlobalExceptionHandler` retornando 400 com mensagem clara. TEST-004 revalidado (agora passa) e TEST-005/TEST-006 adicionados (corpo vazio e regressão do fluxo válido). Status voltou para "Implementado". |
