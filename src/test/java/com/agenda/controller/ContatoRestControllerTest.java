package com.agenda.controller;

import com.agenda.model.Contato;
import com.agenda.service.ContatoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContatoRestController.class)
class ContatoRestControllerTest {

    private static final String JSON = "{\"nome\":\"Ana\",\"telefone\":\"11982781174\","
            + "\"email\":\"ana@empresa.com\",\"departamento\":\"TI\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContatoService service;

    private static Contato contatoSalvo() {
        final Contato c = new Contato("Ana", "11982781174", "ana@empresa.com", "TI");
        c.setId(1L);
        return c;
    }

    @Test
    void listarRetornaOk() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(contatoSalvo()));

        mockMvc.perform(get("/api/contatos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ana"));
    }

    @Test
    void cadastrarRetornaCreated() throws Exception {
        when(service.salvarContato(any(Contato.class))).thenReturn(contatoSalvo());

        mockMvc.perform(post("/api/contatos").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void cadastrarComDadosInvalidosRetornaBadRequest() throws Exception {
        when(service.salvarContato(any(Contato.class)))
                .thenThrow(new IllegalArgumentException("Telefone com tamanho invalido"));

        mockMvc.perform(post("/api/contatos").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Telefone com tamanho invalido"));
    }

    @Test
    void cadastrarRetornaErroInternoQuandoServicoFalha() throws Exception {
        when(service.salvarContato(any(Contato.class))).thenReturn(null);

        mockMvc.perform(post("/api/contatos").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro interno ao salvar."));
    }

    @Test
    void atualizarExistenteRetornaOk() throws Exception {
        when(service.atualizarContato(eq(1L), any(Contato.class))).thenReturn(contatoSalvo());

        mockMvc.perform(put("/api/contatos/1").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"));
    }

    @Test
    void atualizarInexistenteRetornaNotFound() throws Exception {
        when(service.atualizarContato(eq(99L), any(Contato.class))).thenReturn(null);

        mockMvc.perform(put("/api/contatos/99").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarComDadosInvalidosRetornaBadRequest() throws Exception {
        when(service.atualizarContato(eq(1L), any(Contato.class)))
                .thenThrow(new IllegalArgumentException("Email invalido: x"));

        mockMvc.perform(put("/api/contatos/1").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email invalido: x"));
    }

    @Test
    void removerRetornaNoContent() throws Exception {
        mockMvc.perform(delete("/api/contatos/1"))
                .andExpect(status().isNoContent());

        verify(service).deletarContato(1L);
    }
}
