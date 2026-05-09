package com.techchallenge.biblioteca.service;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.techchallenge.biblioteca.dto.RelatorioLivroEmprestadoDTO;
import com.techchallenge.biblioteca.dto.RelatorioLivroMaisEmprestadoDTO;
import com.techchallenge.biblioteca.entity.Emprestimo;
import com.techchallenge.biblioteca.entity.Livro;
import com.techchallenge.biblioteca.entity.Usuario;
import com.techchallenge.biblioteca.enums.StatusEmprestimo;
import com.techchallenge.biblioteca.repository.EmprestimoRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    void deveListarLivrosMaisEmprestados() {
        when(emprestimoRepository.findLivrosMaisEmprestadosComQuantidade(any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[]{1L, "Clean Code", "Robert C. Martin", "ISBN-001", 8L}));

        List<RelatorioLivroMaisEmprestadoDTO> response = relatorioService.listarLivrosMaisEmprestados();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getQuantidadeEmprestimos()).isEqualTo(8L);
        assertThat(response.get(0).getTitulo()).isEqualTo("Clean Code");
    }

    @Test
    void deveListarLivrosEmprestadosNoMomento() {
        when(emprestimoRepository.findEmprestimosAtivosComPrevisaoDevolucao())
                .thenReturn(List.of(criarEmprestimo()));

        List<RelatorioLivroEmprestadoDTO> response = relatorioService.listarLivrosEmprestadosNoMomento();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getTitulo()).isEqualTo("Clean Code");
        assertThat(response.get(0).getNomeUsuario()).isEqualTo("Maria");
    }

    @Test
    void deveFalharAoMapearResultadoAgregadoInvalido() {
        when(emprestimoRepository.findLivrosMaisEmprestadosComQuantidade(any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[]{1L, "Clean Code"}));

        assertThatThrownBy(() -> relatorioService.listarLivrosMaisEmprestados())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Resultado agregado invalido para o relatorio de livros mais emprestados.");
    }

    private Emprestimo criarEmprestimo() {
        Livro livro = new Livro();
        livro.setId(1L);
        livro.setTitulo("Clean Code");
        livro.setAutor("Robert C. Martin");
        livro.setIsbn("ISBN-001");
        livro.setDisponivel(false);

        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setNome("Maria");
        usuario.setEmail("maria@mail.com");

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(3L);
        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(LocalDate.now().minusDays(1));
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(6));
        emprestimo.setStatus(StatusEmprestimo.EMPRESTADO);
        return emprestimo;
    }
}
