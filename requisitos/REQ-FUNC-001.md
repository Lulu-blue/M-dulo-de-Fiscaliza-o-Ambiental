# Ficha de Requisito

> **Norma de referência:** ISO/IEC/IEEE 29148:2018 (Seções 5.2.5 e 5.2.8)

---

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | `REQ-FUNC-001` |
| **Nome** | `Autenticação de usuário com emissão de token JWT` |
| **Tipo** | `Funcional / Segurança` |
| **Prioridade** | `Essencial` |
| **Status** | `Implementado` |
| **Versão** | `1.1` |

---

## Descrição

### Texto do Requisito

O sistema deve autenticar um usuário mediante CPF (ou matrícula) e senha, e, em caso de credenciais válidas, emitir um token JWT assinado contendo o cargo do usuário.

### Condições de Aplicação

- Condição 1: A requisição é enviada ao endpoint `POST /api/auth/login`.
- Condição 2: O usuário está cadastrado na tabela `usuarios` e possui o campo `ativo` igual a verdadeiro.

---

## Rastreabilidade

| Campo | Valor |
|-------|-------|
| **Fonte (Source)** | `SRS_MODULO_FISCALIZACAO_AMBIENTAL_v1.0.md`, Seções 2.2 e 3.2 |
| **Requisito Pai** | — |
| **Requisitos Filhos** | `REQ-SEG-001`, `REQ-SEG-002` |
| **Casos de Uso / Histórias Relacionadas** | UC-LOGIN |

---

## Justificativa (Rationale)

Sem autenticação, não é possível diferenciar as ações permitidas a um Gestor das permitidas a um Fiscal, comprometendo o controle de acesso descrito no plano do projeto e exigido pelo processo seletivo (uso de JWT como padrão de mercado).

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
Funcionalidade: Autenticação de usuário
  Cenário: Login com credenciais válidas
    Dado um usuário ativo cadastrado com CPF e senha válidos
    Quando ele envia POST /api/auth/login com essas credenciais
    Então o sistema responde com status 200 e um token JWT contendo o cargo do usuário

  Cenário: Login com credenciais inválidas
    Dado um usuário que informa uma senha incorreta
    Quando ele envia POST /api/auth/login
    Então o sistema responde com status 401 sem indicar qual campo está incorreto
```

### Casos de Teste Associados
| ID do Teste | Descrição | Resultado Esperado | Resultado Obtido |
|-------------|-----------|--------------------|-------------------|
| TEST-001 | Login com CPF e senha corretos | Status 200 e token JWT válido retornado | ✅ Confirmado repetidas vezes ao longo da sessão |
| TEST-002 | Login com senha incorreta | Status 401, sem token | ✅ Confirmado ("CPF ou senha inválidos.") |
| TEST-003 | Login com usuário inativo (`ativo = false`) | Status 401 | ✅ Confirmado — usuário de teste criado com `ativo=false`, login rejeitado com 401 |
| TEST-004 | Enviar **qualquer** requisição (inclusive o próprio login) com um token JWT expirado no cabeçalho `Authorization` | A requisição não deve quebrar; login deve funcionar normalmente ignorando o token velho | ❌→✅ **Bug real encontrado e corrigido**: `JwtAuthenticationFilter` não tratava `ExpiredJwtException`/`JwtException`, deixando a exceção estourar e ser mascarada como `403` pelo Spring (mesmo padrão de `/error` já visto em outras fichas). Ocorria de verdade em produção: um token de 24h expirado, ainda guardado no `localStorage` do navegador, era reenviado pelo interceptor em toda requisição — inclusive no login. Corrigido com try/catch no filtro (trata como não autenticado) e o interceptor do Angular parou de anexar token em `/api/auth/login` |

Ver `Histórico de Alterações` e a seção **Restrições e Dependências** para o detalhamento do TEST-004.

---

## Análise de Conformidade com a Norma

| Característica | Atende? | Observações |
|----------------|---------|-------------|
| **Necessary (Necessário)** | [x] Sim [ ] Não | Sem login não há como aplicar RBAC nem numeração atribuída a um usuário identificado. |
| **Appropriate (Apropriado)** | [x] Sim [ ] Não | Descreve o comportamento esperado sem prescrever a implementação interna do JWT. |
| **Unambiguous (Não ambíguo)** | [x] Sim [ ] Não | Único fluxo de entrada/saída possível. |
| **Complete (Completo)** | [x] Sim [ ] Não | Não depende de informação externa não referenciada. |
| **Singular (Singular)** | [x] Sim [ ] Não | Trata exclusivamente da autenticação. |
| **Feasible (Factível)** | [x] Sim [ ] Não | Já implementado (`AuthController`, `JwtUtil`). |
| **Verifiable (Verificável)** | [x] Sim [ ] Não | Comprovável por status HTTP e presença do token. |
| **Correct (Correto)** | [x] Sim [ ] Não | Reflete o fluxo real implementado no backend. |
| **Conforming (Conforme)** | [x] Sim [ ] Não | Segue o template aprovado. |

---

## Informações Complementares

### Restrições e Dependências
- Depende de `REQ-SEG-002` (senha armazenada como hash) para validação segura da credencial.
- Restrito ao par CPF/matrícula + senha; não há login social ou multifator nesta versão.
- ~~Gap real: `JwtAuthenticationFilter` não tratava tokens expirados/inválidos...~~ **Corrigido em 13/09/2026.** O filtro agora captura qualquer exceção ao interpretar o token (expirado, malformado, assinatura inválida, usuário do token não existe mais) e trata a requisição como não autenticada, sem deixar a exceção vazar. `auth.interceptor.ts` também não anexa mais o token salvo à chamada de login, e passou a deslogar automaticamente (limpando o token salvo) em qualquer resposta `401`, evitando o usuário ficar preso vendo erros sem entender por quê.
- O token dura 24h (`JwtUtil.EXPIRATION_TIME`); não há refresh token nem renovação automática nesta versão — expirado, o usuário precisa logar de novo.

### Notas e Suposições
- Assume-se que o algoritmo de assinatura e o tempo de expiração do JWT são configurados no backend (`JwtUtil`) e não são renegociáveis pelo cliente.

### Anexos / Referências
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/AuthController.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/security/JwtUtil.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/security/JwtAuthenticationFilter.java`
- `fiscal-ambiental-frontend/src/app/services/auth.interceptor.ts`

---

## Histórico de Alterações

| Versão | Data | Autor | Alteração |
|--------|------|-------|-----------|
| 1.0 | 10/09/2026 | Luiza | Criação inicial |
| 1.1 | 13/09/2026 | Luiza | Verificação completa: TEST-001/002/003 confirmados ao vivo. Encontrado e corrigido bug real (TEST-004): token JWT expirado guardado no navegador derrubava qualquer requisição (inclusive o login) com `403` mascarado. Corrigido no `JwtAuthenticationFilter` e no `auth.interceptor.ts`. |
