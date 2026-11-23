package com.agenda.service;

import com.agenda.model.Contato;
import com.agenda.repository.ContatoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ContatoService {

    private static final Logger LOGGER = LogManager.getLogger(ContatoService.class);

    private static final int TELEFONE_TAMANHO_MINIMO = 8;
    private static final int TELEFONE_TAMANHO_MAXIMO = 14;
    private static final int TELEFONE_TAMANHO_CELULAR = 11;
    private static final String DDD_SAO_PAULO = "11";
    private static final String DOMINIO_INTERNO = "@empresa.com";

    private final ContatoRepository contatoRepository;

    public ContatoService(final ContatoRepository contatoRepository) {
        this.contatoRepository = contatoRepository;
    }

    public List<Contato> listarTodos() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Executando consulta geral de contatos...");
        }
        return contatoRepository.findAll();
    }

    public Optional<Contato> buscarPorId(final Long id) {
        return contatoRepository.findById(id);
    }

    public Contato salvarContato(final Contato contato) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Processando novo contato: {}", contato.getNome());
        }

        validarESanitizarContato(contato, contato);

        return persistir(contato, "salvar");
    }

    public Contato atualizarContato(final Long id, final Contato dadosAtualizados) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Atualizando contato ID: {}", id);
        }

        final Optional<Contato> existente = contatoRepository.findById(id);
        if (existente.isEmpty()) {
            return null;
        }

        final Contato contato = existente.get();

        validarESanitizarContato(dadosAtualizados, contato);

        contato.setNome(dadosAtualizados.getNome());
        contato.setDepartamento(dadosAtualizados.getDepartamento());
        contato.setObservacoes(dadosAtualizados.getObservacoes());

        return persistir(contato, "atualizar");
    }

    public String classificarNivelCriticidadeContato(final Contato contato) {
        if (contato == null) {
            return "BAIXO";
        }

        final String departamento = contato.getDepartamento();
        if (departamento == null) {
            return "INDEFINIDO";
        }

        if ("DIRETORIA".equalsIgnoreCase(departamento)) {
            return classificarDiretoria(contato);
        }
        if ("COMPLIANCE".equalsIgnoreCase(departamento) || "RISCO".equalsIgnoreCase(departamento)) {
            return classificarComplianceRisco(contato);
        }
        if ("TI".equalsIgnoreCase(departamento) || "OPERACIONAL".equalsIgnoreCase(departamento)) {
            return classificarTiOperacional(contato);
        }
        return "OPERACIONAL";
    }

    public void deletarContato(final Long id) {
        try {
            contatoRepository.deleteById(id);
        } catch (DataAccessException e) {
            registrarErro("Falha ao remover contato ID {}: {}", id, e.getMessage());
        }
    }

    public List<Contato> buscarPorDepartamento(final String depto) {
        return contatoRepository.findByDepartamentoIgnoreCase(depto);
    }

    /**
     * Valida e sanitiza telefone e e-mail de {@code origem}, gravando os valores
     * normalizados em {@code destino}. Campos nulos são ignorados.
     */
    private void validarESanitizarContato(final Contato origem, final Contato destino) {
        final String telefone = validarESanitizarTelefone(origem.getTelefone());
        final String email = validarESanitizarEmail(origem.getEmail());
        if (telefone != null) {
            destino.setTelefone(telefone);
        }
        if (email != null) {
            destino.setEmail(email);
        }
    }

    private String validarESanitizarTelefone(final String telefone) {
        if (telefone == null) {
            return null;
        }
        final String fone = telefone.replace("(", "").replace(")", "").replace("-", "").replace(" ", "").trim();
        if (fone.length() < TELEFONE_TAMANHO_MINIMO || fone.length() > TELEFONE_TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("Telefone com tamanho invalido");
        }
        return fone;
    }

    private String validarESanitizarEmail(final String valor) {
        final String email = valor == null ? null : valor.trim().toLowerCase(Locale.ROOT);
        if (email != null && (!email.contains("@") || !email.contains("."))) {
            throw new IllegalArgumentException("Email invalido: " + email);
        }
        return email;
    }

    private void registrarErro(final String mensagem, final Object... argumentos) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(mensagem, argumentos);
        }
    }

    private Contato persistir(final Contato contato, final String acao) {
        try {
            return contatoRepository.save(contato);
        } catch (DataAccessException e) {
            registrarErro("Erro ao {} contato: {}", acao, e.getMessage());
            return null;
        }
    }

    private String classificarDiretoria(final Contato contato) {
        final String telefone = contato.getTelefone();
        return telefone != null && telefone.startsWith(DDD_SAO_PAULO) ? "CRITICO_SP" : "CRITICO_GERAL";
    }

    private String classificarComplianceRisco(final Contato contato) {
        if (contato.getEmail() != null && contato.getEmail().endsWith(DOMINIO_INTERNO)) {
            return "ALTO_INTERNO";
        }
        return "ALTO_EXTERNO";
    }

    private String classificarTiOperacional(final Contato contato) {
        return possuiCelular(contato.getTelefone()) ? "MEDIO_URGENTE" : "MEDIO_PADRAO";
    }

    private boolean possuiCelular(final String telefone) {
        return telefone != null && telefone.length() >= TELEFONE_TAMANHO_CELULAR;
    }
}
