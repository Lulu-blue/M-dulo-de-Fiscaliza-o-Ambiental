# Ficha de Requisito

> **Norma de referência:** ISO/IEC/IEEE 29148:2018 (Seções 5.2.5 e 5.2.8)

---

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | `REQ-DESEMP-002` |
| **Nome** | `Bloqueio client-side de upload de arquivo duplicado` |
| **Tipo** | `Performance` |
| **Prioridade** | `Essencial` |
| **Status** | `Implementado` |
| **Versão** | `1.1` |

---

## Descrição

### Texto do Requisito

O frontend deve calcular o hash SHA-256 de um arquivo selecionado para anexo, utilizando a Web Crypto API, e consultar o backend (`GET /api/anexos/verificar-hash/{hash}`) antes de iniciar o envio; caso o hash já esteja cadastrado, o sistema deve impedir o envio do arquivo ao servidor.

### Condições de Aplicação

- Condição 1: Aplica-se a todo arquivo selecionado para anexação em uma Demanda, previamente ao envio via `POST /api/anexos`.
- Condição 2: O navegador utilizado possui suporte à Web Crypto API.

---

## Rastreabilidade

| Campo | Valor |
|-------|-------|
| **Fonte (Source)** | `SRS_MODULO_FISCALIZACAO_AMBIENTAL_v1.0.md`, Seções 3.4 e 3.8-c5 |
| **Requisito Pai** | `REQ-FUNC-006` |
| **Requisitos Filhos** | — |
| **Casos de Uso / Histórias Relacionadas** | UC-ANEXAR-EVIDENCIA |

---

## Justificativa (Rationale)

Poupa banda e armazenamento do servidor ao evitar o envio de arquivos já existentes, conforme descrito no plano do projeto e destacado no roteiro de estudos da vaga como evidência de "pensar em performance e economia de infraestrutura" — um diferencial pouco comum em candidatos júnior/pleno.

---

## Critérios de Verificação e Aceitação

### Método de Verificação
- [ ] Inspeção / Revisão
- [x] Teste
- [x] Demonstração
- [ ] Análise
- [ ] Simulação

### Critérios de Aceitação (Gherkin / BDD recomendado)
```gherkin
Funcionalidade: Bloqueio client-side de anexo duplicado
  Cenário: Usuário seleciona um arquivo já enviado anteriormente
    Dado um arquivo cujo hash SHA-256 já está cadastrado no backend
    Quando o Fiscal o seleciona para anexar a uma demanda no Angular
    Então o hash é calculado no navegador e a requisição de upload não é enviada ao servidor

  Cenário: Usuário seleciona um arquivo inédito
    Dado um arquivo cujo hash SHA-256 não está cadastrado
    Quando o Fiscal o seleciona para anexar
    Então o upload prossegue normalmente para POST /api/anexos
```

### Casos de Teste Associados
| ID do Teste | Descrição | Resultado Esperado | Resultado Obtido |
|-------------|-----------|--------------------|-------------------|
| TEST-001 | Selecionar arquivo com hash duplicado no painel do Fiscal | Nenhuma requisição de upload é registrada na rede; usuário é avisado | ✅ Confirmado pela tela real (Chrome headless, `DOM.setFileInputFiles` simulando seleção de arquivo de verdade) em `REQ-FUNC-006` v1.1: badge "Duplicidade Detectada no Banco!" aparece e `document.querySelector('.btn-upload').disabled === true` |
| TEST-002 | Selecionar arquivo inédito | Requisição `POST /api/anexos` é disparada normalmente | ✅ Confirmado — primeira seleção do arquivo de teste passou por `verificar-hash` (`existe:false`) e completou o upload com sucesso, aparecendo em "Documentos salvos nesta demanda" |

Esses testes foram executados durante a verificação de `REQ-FUNC-006` (mesma sessão de correção do contrato frontend/backend do anexo); ver aquela ficha para o passo a passo completo.

---

## Análise de Conformidade com a Norma

| Característica | Atende? | Observações |
|----------------|---------|-------------|
| **Necessary (Necessário)** | [x] Sim [ ] Não | Otimização explicitamente definida como diferencial do projeto. |
| **Appropriate (Apropriado)** | [x] Sim [ ] Não | Especifica a API do navegador (Web Crypto) por ser restrição de design já adotada, não imposição arbitrária. |
| **Unambiguous (Não ambíguo)** | [x] Sim [ ] Não | Ordem de operações (hash → consulta → bloqueio/envio) é única. |
| **Complete (Completo)** | [x] Sim [ ] Não | Autocontido, complementa `REQ-FUNC-006`. |
| **Singular (Singular)** | [x] Sim [ ] Não | Trata exclusivamente da verificação prévia no cliente. |
| **Feasible (Factível)** | [x] Sim [ ] Não | Já implementado (`anexo.service.ts`, componentes do painel do Fiscal). |
| **Verifiable (Verificável)** | [x] Sim [ ] Não | Verificável por inspeção do tráfego de rede do navegador. |
| **Correct (Correto)** | [x] Sim [ ] Não | Reflete o comportamento implementado no fluxo de upload de anexo. |
| **Conforming (Conforme)** | [x] Sim [ ] Não | Segue o template. |

---

## Informações Complementares

### Restrições e Dependências
- Depende de `REQ-FUNC-006` (existência do endpoint `verificar-hash` no backend).
- Depende de suporte do navegador à Web Crypto API (disponível nos navegadores modernos, não em contextos não seguros/HTTP sem TLS em produção).

### Notas e Suposições
- Assume-se que a verificação client-side é uma otimização de experiência/banda, e não o único mecanismo de proteção contra duplicidade — o servidor deve revalidar (ver `REQ-FUNC-006`), pois o cliente não é uma fronteira de confiança.

### Anexos / Referências
- `fiscal-ambiental-frontend/src/app/services/anexo.service.ts`
- `fiscal-ambiental-frontend/src/app/components/fiscal-dashboard/fiscal-dashboard.component.ts`

---

## Histórico de Alterações

| Versão | Data | Autor | Alteração |
|--------|------|-------|-----------|
| 1.0 | 10/09/2026 | Luiza | Criação inicial |
| 1.1 | 13/09/2026 | Luiza | TEST-001/002 confirmados pela tela real (não só leitura de código), durante a sessão de correção de `REQ-FUNC-006`. Nenhum gap novo encontrado nesta ficha especificamente. |
