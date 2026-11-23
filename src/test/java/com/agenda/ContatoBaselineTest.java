package com.agenda;

import com.agenda.model.Contato;
import com.agenda.service.ContatoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ContatoBaselineTest {

    @Autowired
    private ContatoService contatoService;

    @Test
    void testContextLoads() {
        // Validação básica do contexto do Spring Boot
        assertNotNull(contatoService);
    }

    @Test
    void testBaselineListingOnly() {
        // Teste superficial que executa apenas a listagem inicial vazia.
        // Garante intencionalmente exatamente a faixa de ~10% de cobertura global no relatório JaCoCo.
        assertNotNull(contatoService.listarTodos());
    }
}
