# Agenda de Contatos - Projeto Prático de Sustentação e Qualidade

Aplicação desenvolvida em **Spring Boot, Spring Data JPA, Spring MVC e Thymeleaf**.

## O que está incluído no pacote:
1. **Telas Web (Spring MVC + Thymeleaf):**
   - Rota `/contatos`: Tela de listagem de contatos cadastrados em tabela com opções de edição e exclusão.
   - Rota `/contatos/novo`: Tela de formulário estilizada com validação e mensagens de alerta.
   - Rota `/contatos/editar/{id}`: Tela de edição de dados existentes.
2. **API REST:**
   - `/api/contatos`: Endpoints RESTful para integração.
3. **Métrica de Cobertura Inicial de ~10%:**
   - A suíte de testes (`ContatoBaselineTest`) testa intencionalmente apenas o carregamento do contexto e a chamada inicial de listagem, gerando exatamente **~10% de cobertura de código no JaCoCo**.
4. **Passivos Técnicos e Falhas de Segurança:**
   - Dependência do **Log4j 2.14.1** com a **CVE-2021-44228 (Log4Shell)**.
   - Validações duplicadas de telefone e e-mail.
   - Método `classificarNivelCriticidadeContato` com alta complexidade ciclomática (~18 horas de dívida técnica).
   - PMD code smells (`System.out.println`, `catch (Exception e)`, atributos mortos).

## Execução
```bash
# Executar a aplicação (Acesse http://localhost:8080/contatos no navegador)
./mvnw spring-boot:run   # ou mvn spring-boot:run

# Rodar testes e inspecionar os 10% de cobertura no JaCoCo
mvn clean test
# Relatório: target/site/jacoco/index.html

# Rodar análise estática do PMD e detecção de código duplicado do CPD
mvn pmd:pmd pmd:cpd
# Relatórios: target/site/pmd.html e target/site/cpd.html
```

## Roteiro de Exercício Prático
Consulte o guia completo com as etapas de refatoração, comandos e critérios de avaliação (100%) em:
- [ROTEIRO_EXERCICIO.md](ROTEIRO_EXERCICIO.md)
