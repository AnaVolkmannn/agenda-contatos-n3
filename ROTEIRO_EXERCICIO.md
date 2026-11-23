# Roteiro de Exercício Prático: Manutenção, Refatoração e Qualidade de Software

## 1. Contexto e Objetivo
Você assumiu a sustentação do sistema legado **Agenda de Contatos** (Spring Boot + Spring Data JPA + Thymeleaf + API REST). O sistema funciona, mas acumula **passivos técnicos graves**:
- **Baixa cobertura de testes (~10%)**, tornando qualquer alteração arriscada;
- **Vulnerabilidade crítica de segurança (Log4Shell - CVE-2021-44228)** nas dependências;
- **Código duplicado** nas regras de validação;
- **Alta complexidade ciclomática e cognitiva** na classificação de criticidade;
- **Code smells** diversos (`System.out.println`, captura genérica de `Exception`, código morto, falta de construtores para injeção, etc.).

**Objetivo da atividade:**
Eliminar a dívida técnica apontada pelas ferramentas **PMD** e **CPD**, corrigir a vulnerabilidade de dependência e elevar a cobertura de testes medida pelo **JaCoCo** de **~10% para no mínimo 80%**, mantendo todas as funcionalidades Web e REST intactas.

---

## 2. Distribuição da Avaliação (100%)

| Atividade | Escopo Principal | Critério de Aceite (Meta) | Peso (%) |
| :--- | :--- | :--- | :---: |
| **Atividade 1** | Diagnóstico Inicial e Correção de Segurança (SCA) | Relatório de baseline registrado e `log4j` atualizado para versão segura (`>= 2.17.1`) no `pom.xml`. | **15%** |
| **Atividade 2** | Eliminação de Código Duplicado (CPD) | Extração de métodos reutilizáveis em `ContatoService`, zerando a duplicação de validação no relatório do CPD. | **20%** |
| **Atividade 3** | Redução de Complexidade Ciclomática e Cognitiva | Refatorar `classificarNivelCriticidadeContato` para Complexidade Ciclomática `< 10` e Cognitiva `< 15` sem alterar a regra de negócio. | **20%** |
| **Atividade 4** | Remoção de Code Smells e Boas Práticas (PMD) | Remover atributos mortos, `System.out.println`, `catch (Exception)` genéricos, `LiteralsFirstInComparisons` e injeção por campo (`@Autowired`). | **15%** |
| **Atividade 5** | Aumento da Cobertura de Testes para `>= 80%` (JaCoCo) | Criar testes unitários e de controladores (`MockMvc`) atingindo **80% ou mais** de cobertura total no relatório JaCoCo. | **30%** |
| **TOTAL** | | | **100%** |

---

## 3. Comandos Essenciais de Inspeção e Diagnóstico

Execute os comandos abaixo na raiz do projeto para gerar os relatórios antes de iniciar e ao longo das refatorações:

```bash
# 1. Compilar, rodar a suíte de testes e gerar o relatório de cobertura do JaCoCo
mvn clean test

# 2. Executar a análise estática de código (PMD) e detecção de duplicação (CPD)
mvn pmd:pmd pmd:cpd

# 3. Executar tudo em um único comando (Testes + JaCoCo + PMD + CPD)
mvn clean test pmd:pmd pmd:cpd

# 4. Subir a aplicação localmente para validação manual (http://localhost:8080/contatos)
mvn spring-boot:run
```

### Onde consultar os relatórios gerados:
- **Relatório JaCoCo (Cobertura HTML):** `target/site/jacoco/index.html`
- **Relatório PMD (Code Smells HTML):** `target/site/pmd.html` (ou XML em `target/pmd.xml`)
- **Relatório CPD (Duplicação HTML):** `target/site/cpd.html` (ou XML em `target/cpd.xml`)

---

## 4. Passo a Passo das Atividades

### Atividade 1: Diagnóstico Inicial e Segurança (15%)
1. Execute `mvn clean test pmd:pmd pmd:cpd` e anote os indicadores iniciais do projeto:
   - Cobertura total do JaCoCo (~10%);
   - Total de violações no `target/site/pmd.html`;
   - Total de blocos duplicados no `target/site/cpd.html`.
2. Inspecione o arquivo `pom.xml` e localize as dependências do **Apache Log4j (`2.14.1`)**, vulneráveis à **CVE-2021-44228 (Log4Shell)**:
   - `log4j-core`
   - `log4j-api`
   - `log4j-slf4j-impl`
3. Atualize a versão dessas dependências para uma versão corrigida (ex.: `2.17.2` ou superior compatível com Java 11) e valide com `mvn clean test`.

---

### Atividade 2: Eliminação de Duplicação de Código - CPD (20%)
1. Abra o relatório `target/site/cpd.html` (ou `target/cpd.xml`).
2. Observe a duplicação de **18 linhas / 146 tokens** em `src/main/java/com/agenda/service/ContatoService.java` entre os métodos `salvarContato` (linhas 35–52) e `atualizarContato` (linhas 70–87).
3. Refatore a classe `ContatoService`:
   - Extraia a lógica de sanitização e validação de **telefone** e **e-mail** para métodos privados dedicados (por exemplo, `validarESanitizarTelefone(String telefone)` e `validarESanitizarEmail(String email)`, ou `validarDadosContato(Contato origem, Contato destino)`).
   - Reutilize a lógica tanto no cadastro quanto na atualização.
4. Rode `mvn pmd:cpd` novamente e confirme que a duplicação em `ContatoService` desapareceu.

---

### Atividade 3: Redução de Complexidade Ciclomática e Cognitiva (20%)
1. No relatório do PMD (`target/site/pmd.html`), localize as violações de **`CyclomaticComplexity` (14)** e **`CognitiveComplexity` (31)** no método `classificarNivelCriticidadeContato(Contato c)` em `ContatoService.java`.
2. Refatore o método aplicando técnicas de *Clean Code*:
   - Utilize **Guard Clauses** (retornos antecipados para `c == null` e `c.getDepartamento() == null`) para eliminar níveis de indentação;
   - Extraia métodos auxiliares por departamento ou utilize estruturas de decisão mais limpas (`switch` / `Map` / métodos privados específicos);
   - Compare strings com literais à esquerda (`"DIRETORIA".equalsIgnoreCase(depto)`) para evitar `NullPointerException` e atender à regra `LiteralsFirstInComparisons`.
3. Rode `mvn pmd:pmd` e verifique que `classificarNivelCriticidadeContato` não apresenta mais alertas de `CyclomaticComplexity` nem de `CognitiveComplexity`.

---

### Atividade 4: Remoção de Code Smells e Boas Práticas - PMD (15%)
Ainda guiado pelo relatório `target/site/pmd.html`, limpe os principais *code smells* nas classes do projeto (`ContatoService`, `ContatoWebController`, `ContatoRestController` e `Contato`):
1. **Código morto (`UnusedPrivateField`):** Remova o atributo não utilizado `defaultCountryCode` em `ContatoService`.
2. **Saída padrão (`SystemPrintln`):** Substitua todos os `System.out.println(...)` por chamadas ao logger (`LOGGER.info(...)` ou `LOGGER.error(...)`).
3. **Convenção de constantes (`FieldNamingConventions`):** Renomeie `logger` para `LOGGER` (por ser `private static final`) e adicione verificações de nível ou mensagens parametrizadas (`LOGGER.info("Processando novo contato: {}", contato.getNome())`).
4. **Exceções genéricas (`AvoidCatchingGenericException`):** Evite `catch (Exception e)` genérico para operações de banco; capture exceções específicas do Spring Data (`DataAccessException`) ou trate adequadamente registrando o erro no `LOGGER`.
5. **Injeção de Dependência e Construtores (`AtLeastOneConstructor`):** Substitua a injeção por atributo (`@Autowired private ...`) por **injeção via construtor** em `ContatoService`, `ContatoWebController` e `ContatoRestController`.
6. **Conversão de caixa com Locale (`UseLocaleWithCaseConversions`):** Use `.toLowerCase(Locale.ROOT)` ao normalizar o e-mail.

---

### Atividade 5: Aumento da Cobertura de Testes para >= 80% - JaCoCo (30%)
Atualmente, a classe `ContatoBaselineTest` cobre apenas ~10% do código. Crie testes automatizados em `src/test/java/com/agenda/` para atingir **no mínimo 80% de cobertura global** no JaCoCo:

1. **Testes de Serviço (`ContatoServiceTest`):**
   - Cadastro e atualização de contato com sucesso (verificando sanitização de parênteses/traços no telefone e conversão de e-mail para minúsculas);
   - Disparo de `IllegalArgumentException` para telefones curtos (`< 8` dígitos) ou longos (`> 14` dígitos);
   - Disparo de `IllegalArgumentException` para e-mails sem `@` ou sem `.`;
   - Atualização de contato com ID inexistente (retorno `null`);
   - Exclusão e busca por departamento;
   - Todos os cenários do método `classificarNivelCriticidadeContato`:
     - Contato `null` (`BAIXO`) e departamento `null` (`INDEFINIDO`);
     - `DIRETORIA` com DDD `11` (`CRITICO_SP`) e outros DDDs (`CRITICO_GERAL`);
     - `COMPLIANCE` / `RISCO` com `@empresa.com` (`ALTO_INTERNO`) e externo (`ALTO_EXTERNO`);
     - `TI` / `OPERACIONAL` com celular `>= 11` dígitos (`MEDIO_URGENTE`) e fixo (`MEDIO_PADRAO`);
     - Outros departamentos (`OPERACIONAL`).
2. **Testes da API REST (`ContatoRestControllerTest` com `@WebMvcTest` ou `@AutoConfigureMockMvc`):**
   - `GET /api/contatos` (200 OK);
   - `POST /api/contatos` com dados válidos (201 Created) e inválidos (400 Bad Request);
   - `PUT /api/contatos/{id}` existente (200 OK), inexistente (404 Not Found) e inválido (400 Bad Request);
   - `DELETE /api/contatos/{id}` (204 No Content).
3. **Testes do Controller Web (`ContatoWebControllerTest` com `MockMvc`):**
   - Rotas `GET /`, `GET /contatos`, `GET /contatos/novo`, `GET /contatos/editar/{id}` (existente e inexistente);
   - Submissão `POST /contatos/salvar` e `POST /contatos/atualizar/{id}` (cenários válidos com redirecionamento e inválidos retornando à view de formulário);
   - Exclusão `GET /contatos/excluir/{id}`.

---

## 5. Checklist de Validação Final (Entrega)

Antes de entregar, execute o comando completo de validação:

```bash
mvn clean test pmd:pmd pmd:cpd
```

Verifique se todos os itens abaixo foram atendidos:
- [ ] Todos os testes passam sem erros (`BUILD SUCCESS`).
- [ ] O relatório `target/site/jacoco/index.html` indica **Total >= 80%** na coluna *Instructions* (e *Lines*).
- [ ] O relatório `target/site/cpd.html` não aponta mais duplicação de lógica de validação em `ContatoService`.
- [ ] O relatório `target/site/pmd.html` não apresenta violações de `CyclomaticComplexity`, `CognitiveComplexity`, `SystemPrintln`, `UnusedPrivateField` ou `AvoidCatchingGenericException` em `ContatoService`.
- [ ] As dependências do Log4j no `pom.xml` foram atualizadas para versão segura.

