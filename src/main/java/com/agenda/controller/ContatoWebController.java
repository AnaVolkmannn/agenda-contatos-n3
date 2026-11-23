package com.agenda.controller;

import com.agenda.model.Contato;
import com.agenda.service.ContatoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/contatos")
public class ContatoWebController {

    private final ContatoService contatoService;

    public ContatoWebController(ContatoService contatoService) {
        this.contatoService = contatoService;
    }

    // Tela 1: Listagem de contatos
    @GetMapping
    public String listarContatos(Model model) {
        model.addAttribute("contatos", contatoService.listarTodos());
        return "contatos/lista";
    }

    // Tela 2: Formulário de novo contato
    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        model.addAttribute("contato", new Contato());
        model.addAttribute("modo", "cadastrar");
        return "contatos/formulario";
    }

    // Ação: Salvar novo contato
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Contato contato, Model model) {
        try {
            contatoService.salvarContato(contato);
            return "redirect:/contatos";
        } catch (IllegalArgumentException ex) {
            return formularioComErro(model, contato, ex, "cadastrar");
        }
    }

    // Tela 3: Formulário de edição
    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Optional<Contato> opt = contatoService.buscarPorId(id);
        if (opt.isEmpty()) {
            return "redirect:/contatos";
        }
        model.addAttribute("contato", opt.get());
        model.addAttribute("modo", "editar");
        return "contatos/formulario";
    }

    // Ação: Atualizar contato
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute Contato contato, Model model) {
        try {
            contatoService.atualizarContato(id, contato);
            return "redirect:/contatos";
        } catch (IllegalArgumentException ex) {
            contato.setId(id);
            return formularioComErro(model, contato, ex, "editar");
        }
    }

    // Ação: Excluir contato
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        contatoService.deletarContato(id);
        return "redirect:/contatos";
    }

    private String formularioComErro(final Model model, final Contato contato,
                                     final IllegalArgumentException ex, final String modo) {
        model.addAttribute("erro", ex.getMessage());
        model.addAttribute("contato", contato);
        model.addAttribute("modo", modo);
        return "contatos/formulario";
    }

    // Redirecionamento da raiz
    @GetMapping("/")
    public String index() {
        return "redirect:/contatos";
    }
}
