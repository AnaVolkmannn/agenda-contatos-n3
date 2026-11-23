# Relatório de Entrega – Agenda de Contatos

## Atividade 1 – Baseline e segurança
**Baseline medido no projeto original** (`mvn clean test pmd:pmd pmd:cpd`, Java 21, PMD 6.49.0):
- JaCoCo: 29 de 543 instruções cobertas (≈ 5,3%); 10 de 146 linhas.
- PMD (prioridade ≤ 2): 8 violações — `FieldNamingConventions` (1), `VariableNamingConventions` (1), `SystemPrintln` (4), `GuardLogStatement` (2).
- CPD: 5 blocos duplicados — `ContatoService` (18 linhas/146 tokens), `Contato` (2, getters/setters), `ContatoRestController` (1), `ContatoWebController` (1).

**Segurança:** `log4j-core`, `log4j-api` e `log4j-slf4j-impl` passaram de `2.14.1` para `2.17.2` (propriedade `log4j2.version` no `pom.xml`, que também sobrescreve o BOM do Spring Boot).

## Atividade 2 – CPD
`salvarContato` e `atualizarContato` agora reutilizam `validarESanitizarContato`, `validarESanitizarTelefone`, `validarESanitizarEmail` e `persistir`.

Demais blocos do CPD: `ContatoRestController` passou a tratar `IllegalArgumentException` num único `@ExceptionHandler` (mesmas respostas HTTP); `ContatoWebController` extraiu `formularioComErro`; em `Contato` os getters/setters ficam entre `// CPD-OFF` e `// CPD-ON` (código estruturalmente idêntico por natureza em POJOs).

## Atividade 3 – Complexidade
`classificarNivelCriticidadeContato` usa guard clauses + um método privado por departamento (`classificarDiretoria`, `classificarComplianceRisco`, `classificarTiOperacional`). Ciclomática estimada ≈ 8 e cognitiva ≈ 7. Regra de negócio inalterada (comparada com a versão original em 2.464 combinações de entrada, 0 divergências).

## Atividade 4 – PMD
- `GuardLogStatement`: chamadas de log protegidas por `isInfoEnabled()`/`isErrorEnabled()`;
- Removido `defaultCountryCode`; `System.out.println` → `LOGGER`; `logger` → `LOGGER` com mensagens parametrizadas;
- `catch (Exception)` → `catch (DataAccessException)` com log de erro;
- Injeção por construtor em `ContatoService`, `ContatoWebController`, `ContatoRestController`;
- `toLowerCase(Locale.ROOT)`; literais à esquerda nas comparações; imports explícitos e parâmetros `final`.

## Atividade 5 – Testes
- `ContatoServiceTest` (Mockito + testes parametrizados): salvar/atualizar, sanitização, limites 8/14 dígitos, e-mail inválido, ID inexistente, falhas de repositório, exclusão, busca por departamento e todos os ramos de classificação.
- `ContatoRestControllerTest` (`@WebMvcTest`): 200, 201, 400, 500, 404, 204.
- `ContatoWebControllerTest` (`@WebMvcTest`): todas as rotas, redirecionamentos e retorno ao formulário em caso de erro.
- `ContatoBaselineTest` mantido (carrega o contexto completo).

## Resultados finais (medidos com `mvn clean test pmd:pmd pmd:cpd`)

| Indicador | Baseline (original) | Final | Meta do roteiro |
| :--- | :---: | :---: | :---: |
| Cobertura JaCoCo – Instructions | 29 de 543 (≈ 5,3%) | **541 de 546 (99%)** | ≥ 80% |
| Cobertura JaCoCo – Lines | 10 de 146 (≈ 7%) | **144 de 146 (98%)** | ≥ 80% |
| Cobertura JaCoCo – Branches | 0 de 58 (0%) | 58 de 62 (93%) | – |
| Violações PMD (prioridade ≤ 2) | 8 | **0** | 0 em `CyclomaticComplexity`, `CognitiveComplexity`, `SystemPrintln`, `UnusedPrivateField`, `AvoidCatchingGenericException` |
| Blocos duplicados no CPD | 5 | **0** | 0 no `ContatoService` |
| Log4j | 2.14.1 (CVE-2021-44228) | **2.17.2** | ≥ 2.17.1 |

Pacotes no JaCoCo: `service` 100% das instruções (92% dos branches), `controller` 100%, `model` 100%. O que restou sem cobertura é o `main()` da `AgendaApplication` e 4 branches do `ContatoService`.

## Como reproduzir
```
mvn clean test pmd:pmd pmd:cpd
```
Relatórios: `target/site/jacoco/index.html`, `target/site/pmd.html` (`target/pmd.xml`) e `target/site/cpd.html` (`target/cpd.xml`). A pasta `target/` não é incluída na entrega por ser gerada pelo build.
