package com.agenda.service;

import com.agenda.model.Contato;
import com.agenda.repository.ContatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContatoServiceTest {

    @Mock
    private ContatoRepository repository;

    private ContatoService service;

    @BeforeEach
    void setUp() {
        service = new ContatoService(repository);
    }

    private static Contato contato(String nome, String telefone, String email, String depto) {
        return new Contato(nome, telefone, email, depto);
    }

    // ---------- listagem / busca ----------

    @Test
    void listarTodosDelegaAoRepositorio() {
        final List<Contato> lista = List.of(contato("A", "11999999999", "a@b.com", "TI"));
        when(repository.findAll()).thenReturn(lista);

        assertSame(lista, service.listarTodos());
    }

    @Test
    void buscarPorIdDelegaAoRepositorio() {
        final Contato c = contato("A", "11999999999", "a@b.com", "TI");
        when(repository.findById(1L)).thenReturn(Optional.of(c));

        assertEquals(Optional.of(c), service.buscarPorId(1L));
    }

    @Test
    void buscarPorDepartamentoDelegaAoRepositorio() {
        final List<Contato> lista = List.of(contato("A", "11999999999", "a@b.com", "TI"));
        when(repository.findByDepartamentoIgnoreCase("ti")).thenReturn(lista);

        assertSame(lista, service.buscarPorDepartamento("ti"));
    }

    // ---------- salvar ----------

    @Test
    void salvarSanitizaTelefoneENormalizaEmail() {
        when(repository.save(any(Contato.class))).thenAnswer(inv -> inv.getArgument(0));

        final Contato salvo = service.salvarContato(
                contato("Ana", " (11) 98278-1174 ", "  ANA@Empresa.COM ", "TI"));

        assertEquals("11982781174", salvo.getTelefone());
        assertEquals("ana@empresa.com", salvo.getEmail());
    }

    @Test
    void salvarAceitaTelefoneEEmailNulos() {
        when(repository.save(any(Contato.class))).thenAnswer(inv -> inv.getArgument(0));

        final Contato salvo = service.salvarContato(contato("Ana", null, null, "TI"));

        assertNull(salvo.getTelefone());
        assertNull(salvo.getEmail());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "(11) 1234", "123456789012345"})
    void salvarRejeitaTelefoneComTamanhoInvalido(String telefone) {
        final Contato c = contato("Ana", telefone, "ana@empresa.com", "TI");

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.salvarContato(c));

        assertEquals("Telefone com tamanho invalido", ex.getMessage());
        verify(repository, never()).save(any(Contato.class));
    }

    @Test
    void salvarAceitaTelefonesNosLimites() {
        when(repository.save(any(Contato.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("12345678", service.salvarContato(contato("A", "12345678", null, "TI")).getTelefone());
        assertEquals("12345678901234",
                service.salvarContato(contato("B", "12345678901234", null, "TI")).getTelefone());
    }

    @ParameterizedTest
    @ValueSource(strings = {"semarroba.com", "sem@ponto", "invalido"})
    void salvarRejeitaEmailInvalido(String email) {
        final Contato c = contato("Ana", "11982781174", email, "TI");

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.salvarContato(c));

        assertEquals("Email invalido: " + email, ex.getMessage());
        verify(repository, never()).save(any(Contato.class));
    }

    @Test
    void salvarRetornaNuloQuandoRepositorioFalha() {
        when(repository.save(any(Contato.class))).thenThrow(new DataIntegrityViolationException("falha"));

        assertNull(service.salvarContato(contato("Ana", "11982781174", "ana@empresa.com", "TI")));
    }

    // ---------- atualizar ----------

    @Test
    void atualizarAplicaDadosSanitizados() {
        final Contato existente = contato("Antigo", "11111111111", "antigo@empresa.com", "RISCO");
        existente.setId(7L);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Contato.class))).thenAnswer(inv -> inv.getArgument(0));

        final Contato novos = contato("Novo", "(21) 91234-5678", "NOVO@Empresa.com", "TI");
        novos.setObservacoes("obs");

        final Contato atualizado = service.atualizarContato(7L, novos);

        assertSame(existente, atualizado);
        assertEquals("Novo", atualizado.getNome());
        assertEquals("21912345678", atualizado.getTelefone());
        assertEquals("novo@empresa.com", atualizado.getEmail());
        assertEquals("TI", atualizado.getDepartamento());
        assertEquals("obs", atualizado.getObservacoes());
    }

    @Test
    void atualizarMantemTelefoneEEmailQuandoNaoInformados() {
        final Contato existente = contato("Antigo", "11111111111", "antigo@empresa.com", "RISCO");
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Contato.class))).thenAnswer(inv -> inv.getArgument(0));

        final Contato atualizado = service.atualizarContato(7L, contato("Novo", null, null, "TI"));

        assertEquals("11111111111", atualizado.getTelefone());
        assertEquals("antigo@empresa.com", atualizado.getEmail());
        assertEquals("Novo", atualizado.getNome());
    }

    @Test
    void atualizarRetornaNuloQuandoIdNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.atualizarContato(99L, contato("X", "11982781174", "x@y.com", "TI")));
        verify(repository, never()).save(any(Contato.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "123456789012345"})
    void atualizarRejeitaTelefoneInvalido(String telefone) {
        when(repository.findById(1L)).thenReturn(Optional.of(contato("A", "11982781174", "a@b.com", "TI")));
        final Contato dados = contato("A", telefone, "a@b.com", "TI");

        assertThrows(IllegalArgumentException.class, () -> service.atualizarContato(1L, dados));
        verify(repository, never()).save(any(Contato.class));
    }

    @Test
    void atualizarRejeitaEmailInvalido() {
        when(repository.findById(1L)).thenReturn(Optional.of(contato("A", "11982781174", "a@b.com", "TI")));
        final Contato dados = contato("A", "11982781174", "invalido", "TI");

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.atualizarContato(1L, dados));

        assertEquals("Email invalido: invalido", ex.getMessage());
    }

    @Test
    void atualizarRetornaNuloQuandoRepositorioFalha() {
        when(repository.findById(1L)).thenReturn(Optional.of(contato("A", "11982781174", "a@b.com", "TI")));
        when(repository.save(any(Contato.class))).thenThrow(new DataIntegrityViolationException("falha"));

        assertNull(service.atualizarContato(1L, contato("B", "11982781174", "b@b.com", "TI")));
    }

    // ---------- exclusão ----------

    @Test
    void deletarDelegaAoRepositorio() {
        service.deletarContato(3L);

        verify(repository).deleteById(3L);
    }

    @Test
    void deletarNaoPropagaExcecaoDeAcessoADados() {
        final DataAccessException erro = new EmptyResultDataAccessException(1);
        doThrow(erro).when(repository).deleteById(404L);

        assertDoesNotThrow(() -> service.deletarContato(404L));
    }

    // ---------- classificação de criticidade ----------

    @Test
    void classificaContatoNuloComoBaixo() {
        assertEquals("BAIXO", service.classificarNivelCriticidadeContato(null));
    }

    @Test
    void classificaDepartamentoNuloComoIndefinido() {
        assertEquals("INDEFINIDO", service.classificarNivelCriticidadeContato(contato("A", "11999999999", "a@b.com", null)));
    }

    @ParameterizedTest
    @CsvSource({
            "DIRETORIA, 11987654321, CRITICO_SP",
            "diretoria, 11987654321, CRITICO_SP",
            "DIRETORIA, 21987654321, CRITICO_GERAL",
            "Diretoria, , CRITICO_GERAL"
    })
    void classificaDiretoria(String depto, String telefone, String esperado) {
        assertEquals(esperado,
                service.classificarNivelCriticidadeContato(contato("A", telefone, "a@empresa.com", depto)));
    }

    @ParameterizedTest
    @CsvSource({
            "COMPLIANCE, ana@empresa.com, ALTO_INTERNO",
            "RISCO, ana@empresa.com, ALTO_INTERNO",
            "risco, ana@empresa.com, ALTO_INTERNO",
            "COMPLIANCE, ana@outra.com, ALTO_EXTERNO",
            "RISCO, , ALTO_EXTERNO"
    })
    void classificaComplianceERisco(String depto, String email, String esperado) {
        assertEquals(esperado,
                service.classificarNivelCriticidadeContato(contato("A", "11987654321", email, depto)));
    }

    @ParameterizedTest
    @CsvSource({
            "TI, 11987654321, MEDIO_URGENTE",
            "OPERACIONAL, 21987654321, MEDIO_URGENTE",
            "ti, 1133334444, MEDIO_PADRAO",
            "OPERACIONAL, 1133334444, MEDIO_PADRAO",
            "TI, , MEDIO_PADRAO"
    })
    void classificaTiEOperacional(String depto, String telefone, String esperado) {
        assertEquals(esperado,
                service.classificarNivelCriticidadeContato(contato("A", telefone, "a@empresa.com", depto)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Investimentos", "Marketing", ""})
    void classificaOutrosDepartamentosComoOperacional(String depto) {
        assertEquals("OPERACIONAL",
                service.classificarNivelCriticidadeContato(contato("A", "11987654321", "a@empresa.com", depto)));
    }
}
