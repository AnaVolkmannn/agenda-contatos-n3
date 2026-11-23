package com.agenda.controller;

import com.agenda.model.Contato;
import com.agenda.service.ContatoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ContatoWebController.class)
class ContatoWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContatoService service;

    private static Contato contato() {
        final Contato c = new Contato("Ana", "11982781174", "ana@empresa.com", "TI");
        c.setId(1L);
        return c;
    }

    @Test
    void raizRedirecionaParaListagem() throws Exception {
        mockMvc.perform(get("/contatos/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contatos"));
    }

    @Test
    void listarExibeContatos() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(contato()));

        mockMvc.perform(get("/contatos"))
                .andExpect(status().isOk())
                .andExpect(view().name("contatos/lista"))
                .andExpect(model().attributeExists("contatos"));
    }

    @Test
    void formularioNovoExibeFormularioVazio() throws Exception {
        mockMvc.perform(get("/contatos/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("contatos/formulario"))
                .andExpect(model().attribute("modo", "cadastrar"))
                .andExpect(model().attributeExists("contato"));
    }

    @Test
    void editarExistenteExibeFormularioPreenchido() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(Optional.of(contato()));

        mockMvc.perform(get("/contatos/editar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("contatos/formulario"))
                .andExpect(model().attribute("modo", "editar"))
                .andExpect(model().attributeExists("contato"));
    }

    @Test
    void editarInexistenteRedirecionaParaListagem() throws Exception {
        when(service.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/contatos/editar/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contatos"));
    }

    @Test
    void salvarValidoRedirecionaParaListagem() throws Exception {
        mockMvc.perform(post("/contatos/salvar")
                        .param("nome", "Ana")
                        .param("telefone", "11982781174")
                        .param("email", "ana@empresa.com")
                        .param("departamento", "TI"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contatos"));

        verify(service).salvarContato(any(Contato.class));
    }

    @Test
    void salvarInvalidoRetornaAoFormulario() throws Exception {
        when(service.salvarContato(any(Contato.class)))
                .thenThrow(new IllegalArgumentException("Telefone com tamanho invalido"));

        mockMvc.perform(post("/contatos/salvar")
                        .param("nome", "Ana")
                        .param("telefone", "123")
                        .param("email", "ana@empresa.com")
                        .param("departamento", "TI"))
                .andExpect(status().isOk())
                .andExpect(view().name("contatos/formulario"))
                .andExpect(model().attribute("erro", "Telefone com tamanho invalido"))
                .andExpect(model().attribute("modo", "cadastrar"));
    }

    @Test
    void atualizarValidoRedirecionaParaListagem() throws Exception {
        mockMvc.perform(post("/contatos/atualizar/1")
                        .param("nome", "Ana")
                        .param("telefone", "11982781174")
                        .param("email", "ana@empresa.com")
                        .param("departamento", "TI"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contatos"));

        verify(service).atualizarContato(eq(1L), any(Contato.class));
    }

    @Test
    void atualizarInvalidoRetornaAoFormulario() throws Exception {
        when(service.atualizarContato(eq(1L), any(Contato.class)))
                .thenThrow(new IllegalArgumentException("Email invalido: x"));

        mockMvc.perform(post("/contatos/atualizar/1")
                        .param("nome", "Ana")
                        .param("telefone", "11982781174")
                        .param("email", "x")
                        .param("departamento", "TI"))
                .andExpect(status().isOk())
                .andExpect(view().name("contatos/formulario"))
                .andExpect(model().attribute("erro", "Email invalido: x"))
                .andExpect(model().attribute("modo", "editar"));
    }

    @Test
    void excluirRedirecionaParaListagem() throws Exception {
        mockMvc.perform(get("/contatos/excluir/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contatos"));

        verify(service).deletarContato(1L);
    }
}
