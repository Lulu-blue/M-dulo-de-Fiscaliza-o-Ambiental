# Ficha de Requisito

> **Norma de referência:** ISO/IEC/IEEE 29148:2018 (Seções 5.2.5 e 5.2.8)

---

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | `REQ-FUNC-006` |
| **Nome** | `Upload de anexo com verificação de duplicidade por hash` |
| **Tipo** | `Funcional` |
| **Prioridade** | `Essencial` |
| **Status** | `Implementado` |
| **Versão** | `1.1` |

---

## Descrição

### Texto do Requisito

O sistema deve permitir que um Fiscal envie arquivos (fotos, laudos em PDF/JPG) como evidência vinculada a uma Demanda, registrando nome do arquivo, hash SHA-256, tamanho em bytes e data de upload.

### Condições de Aplicação

- Condição 1: A requisição é enviada por um usuário autenticado.
- Condição 2: A Demanda referenciada existe.
- Condição 3: A requisição é enviada a `POST /api/anexos`.

---

## Rastreabilidade

| Campo | Valor |
|-------|-------|
| **Fonte (Source)** | `SRS_MODULO_FISCALIZACAO_AMBIENTAL_v1.0.md`, Seções 2.2, 3.1 e 3.5 |
| **Requisito Pai** | `REQ-FUNC-003` |
| **Requisitos Filhos** | `REQ-DESEMP-002` |
| **Casos de Uso / Histórias Relacionadas** | UC-ANEXAR-EVIDENCIA |

---

## Justificativa (Rationale)

Evidências fotográficas e documentais são parte obrigatória de uma vistoria ambiental; o registro do hash SHA-256 junto ao anexo é o que viabiliza a deduplicação tratada em `REQ-DESEMP-002`.

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
Funcionalidade: Upload de anexo
  Cenário: Fiscal envia um novo anexo
    Dado uma demanda existente e um arquivo cujo hash SHA-256 ainda não está cadastrado
    Quando o Fiscal envia POST /api/anexos com o arquivo e a demanda
    Então o sistema registra o anexo com nome, hash, tamanho e data de upload

  Cenário: Servidor recebe anexo com hash já cadastrado
    Dado um arquivo cujo hash SHA-256 já existe na base
    Quando a requisição chega diretamente a POST /api/anexos
    Então o sistema rejeita o upload, mesmo que o cliente não tenha bloqueado previamente
```

### Casos de Teste Associados
| ID do Teste | Descrição | Resultado Esperado | Resultado Obtido |
|-------------|-----------|--------------------|-------------------|
| TEST-001 | Upload de arquivo novo via `POST /api/anexos` (API direta) | Status 200, anexo persistido com hash correto | ✅ Confirmado |
| TEST-002 | Upload de arquivo com hash já existente (via API diretamente) | Rejeição pelo servidor (revalidação server-side) | ✅ Confirmado — `400 Bad Request`, "Arquivo duplicado detectado no sistema pelo hash SHA-256." |
| TEST-003 | Consulta `GET /api/anexos/verificar-hash/{hash}` para hash existente/inexistente | Retorna indicação de duplicidade | ⚠️ Formato do corpo corrigido — ver TEST-004 |
| TEST-004 | Fluxo completo pela **tela real** do Fiscal (não só API): selecionar arquivo no `<input type="file">`, aguardar cálculo de SHA-256 e checagem de duplicidade, clicar em "Enviar para o Backend" | Anexo aparece em "Documentos Salvos nesta Demanda" | ❌→✅ **Bug real encontrado e corrigido** (ver Restrições e Dependências) — antes da correção, o upload pela tela falhava silenciosamente (404) e a detecção de duplicidade no cliente nunca funcionava |
| TEST-005 | Selecionar o mesmo arquivo já enviado, pela tela | Badge "Duplicidade Detectada no Banco!" aparece e o botão "Enviar para o Backend" fica desabilitado | ✅ Confirmado (`duplicadoDetectado: true`, `button.disabled === true`) |

---

## Análise de Conformidade com a Norma

| Característica | Atende? | Observações |
|----------------|---------|-------------|
| **Necessary (Necessário)** | [x] Sim [ ] Não | Etapa 4 do fluxo principal descrito no plano do projeto. |
| **Appropriate (Apropriado)** | [x] Sim [ ] Não | Não define formatos de arquivo aceitos em detalhe de UI. |
| **Unambiguous (Não ambíguo)** | [x] Sim [ ] Não | Campos de registro do anexo claramente listados. |
| **Complete (Completo)** | [x] Sim [ ] Não | Autocontido, referenciando o modelo de dados da Seção 3.5 do SRS. |
| **Singular (Singular)** | [x] Sim [ ] Não | Trata apenas do registro do anexo (a prevenção client-side está em `REQ-DESEMP-002`). |
| **Feasible (Factível)** | [x] Sim [ ] Não | Implementado (`AnexoController`, `AnexoService`) e agora também testado pela tela real, não só pela API. |
| **Verifiable (Verificável)** | [x] Sim [ ] Não | Verificado pelos campos persistidos, pela resposta de `verificar-hash`, e pelo estado interno do componente Angular via `window.ng.getComponent()` (TEST-001 a TEST-005). |
| **Correct (Correto)** | [x] Sim [ ] Não | Reflete `Anexo.java` e, após a correção, também reflete o contrato real consumido pelo frontend (`anexo.service.ts`). |
| **Conforming (Conforme)** | [x] Sim [ ] Não | Segue o template. |

---

## Informações Complementares

### Restrições e Dependências
- Depende de `REQ-FUNC-002`/`REQ-FUNC-003` (existência da Demanda).
- Complementado por `REQ-DESEMP-002` (bloqueio no cliente antes do envio).

### Notas e Suposições
- Assume-se que o cálculo de hash SHA-256 é revalidado no servidor mesmo quando o cliente já o calculou, evitando burlar a deduplicação por chamadas diretas à API.
- Esta versão registra apenas **metadados** do arquivo (nome, hash, tamanho) — não há campo `caminho_arquivo` em `anexos` (diferente de `relatorio_imagens`, que já prevê isso) nem armazenamento de bytes em disco/blob storage. Decisão de escopo aceita para o portfólio: o valor demonstrado é a lógica de dedup por hash, não um sistema de arquivos completo.

### Restrições e Dependências (correção aplicada)
- ~~Bug real (contrato frontend/backend divergente)~~ **Corrigido em 11/09/2026.** Dois problemas foram encontrados e corrigidos juntos:
  1. `anexo.service.ts` chamava `POST /api/anexos/upload` com `FormData` (multipart, incluindo o arquivo bruto); o backend só mapeia `POST /api/anexos` (raiz) esperando JSON (`{demandaId, nomeArquivo, hashSha256, tamanhoBytes}`) — o upload pela tela real resultava em `404`. Corrigido ajustando `fazerUpload()` no frontend para enviar JSON à rota correta, já que o backend nunca armazenou os bytes do arquivo mesmo (item acima).
  2. `AnexoController.verificarHash()` retornava um `boolean` cru (`true`/`false`) como corpo da resposta, mas o frontend esperava `{"existe": boolean}` e lia `res.existe` — que resultava sempre em `undefined` (falsy), ou seja, **a detecção de duplicidade no cliente nunca disparava**, mesmo reenviando um arquivo já cadastrado. Corrigido fazendo o backend responder `{"existe": <boolean>}`.
  - Validado end-to-end pela tela real (Chrome headless via CDP, simulando seleção de arquivo em `<input type="file">`): hash calculado, checagem de duplicidade correta na primeira tentativa (`existe: false`), upload bem-sucedido, badge de duplicidade e botão desabilitado corretamente na segunda tentativa com o mesmo arquivo.

### Anexos / Referências
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/controller/AnexoController.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/service/AnexoService.java`
- `Fiscal-Ambiental/src/main/java/com/portifolio/fiscalambiental/model/Anexo.java`
- `fiscal-ambiental-frontend/src/app/services/anexo.service.ts`
- `fiscal-ambiental-frontend/src/app/components/fiscal-dashboard/fiscal-dashboard.component.ts`

---

## Histórico de Alterações

| Versão | Data | Autor | Alteração |
|--------|------|-------|-----------|
| 1.0 | 10/09/2026 | Luiza | Criação inicial |
| 1.1 | 11/09/2026 | Luiza | Testado pela tela real (não só API) pela primeira vez. Encontrado e corrigido bug real: rota/tipo de conteúdo divergentes entre frontend e backend no upload (`/api/anexos/upload` FormData vs `/api/anexos` JSON), e formato de resposta divergente em `verificar-hash` (boolean cru vs `{existe: boolean}`) — esse segundo bug fazia a detecção de duplicidade no cliente nunca funcionar. TEST-004 e TEST-005 adicionados, validados via Chrome headless simulando a seleção real de arquivo. |
