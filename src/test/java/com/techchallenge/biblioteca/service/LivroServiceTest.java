package com.techchallenge.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techchallenge.biblioteca.dto.LivroRequestDTO;
import com.techchallenge.biblioteca.dto.LivroResponseDTO;
import com.techchallenge.biblioteca.entity.Livro;
import com.techchallenge.biblioteca.exception.BusinessException;
import com.techchallenge.biblioteca.exception.ResourceNotFoundException;
import com.techchallenge.biblioteca.repository.EmprestimoRepository;
import com.techchallenge.biblioteca.repository.LivroRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @InjectMocks
    private LivroService livroService;

    @Test
    void deveCadastrarLivroComDadosNormalizados() {
        LivroRequestDTO request = new LivroRequestDTO(" Clean Code ", " Robert C. Martin ", " isbn-001 ");

        when(livroRepository.findByIsbnIgnoreCase("ISBN-001")).thenReturn(Optional.empty());
        when(livroRepository.save(any(Livro.class))).thenAnswer(invocation -> {
            Livro livro = invocation.getArgument(0);
            livro.setId(1L);
            return livro;
        });

        LivroResponseDTO response = livroService.cadastrarLivro(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitulo()).isEqualTo("Clean Code");
        assertThat(response.getAutor()).isEqualTo("Robert C. Martin");
        assertThat(response.getIsbn()).isEqualTo("ISBN-001");
        assertThat(response.getDisponivel()).isTrue();
    }

    @Test
    void deveLancarBusinessExceptionAoCadastrarLivroComIsbnDuplicado() {
        LivroRequestDTO request = new LivroRequestDTO("Livro", "Autor", "isbn-001");

        when(livroRepository.findByIsbnIgnoreCase("ISBN-001")).thenReturn(Optional.of(criarLivro(2L, "ISBN-001")));

        assertThatThrownBy(() -> livroService.cadastrarLivro(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ja existe um livro cadastrado com o ISBN informado.");

        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void deveListarLivrosPaginados() {
        Livro livro = criarLivro(1L, "ISBN-001");
        PageRequest pageable = PageRequest.of(0, 10);

        when(livroRepository.buscarComFiltros("Clean", "Martin", "isbn", pageable))
                .thenReturn(new PageImpl<>(List.of(livro), pageable, 1));

        Page<LivroResponseDTO> response = livroService.listarLivros("Clean", "Martin", "isbn", pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitulo()).isEqualTo("Livro 1");
    }

    @Test
    void deveBuscarLivroPorId() {
        when(livroRepository.findById(1L)).thenReturn(Optional.of(criarLivro(1L, "ISBN-001")));

        LivroResponseDTO response = livroService.buscarLivroPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getIsbn()).isEqualTo("ISBN-001");
    }

    @Test
    void deveLancarNotFoundAoBuscarLivroInexistente() {
        when(livroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> livroService.buscarLivroPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Livro com id 99 nao encontrado.");
    }

    @Test
    void deveAtualizarLivroSemAlterarDisponibilidadeQuandoIsbnPertenceAoMesmoRegistro() {
        Livro livro = criarLivro(1L, "ISBN-001");
        livro.setDisponivel(false);
        LivroRequestDTO request = new LivroRequestDTO(" Refactoring ", " Martin Fowler ", " isbn-001 ");

        when(livroRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(livro));
        when(livroRepository.findByIsbnIgnoreCase("ISBN-001")).thenReturn(Optional.of(livro));
        when(livroRepository.save(any(Livro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LivroResponseDTO response = livroService.atualizarLivro(1L, request);

        assertThat(response.getTitulo()).isEqualTo("Refactoring");
        assertThat(response.getAutor()).isEqualTo("Martin Fowler");
        assertThat(response.getIsbn()).isEqualTo("ISBN-001");
        assertThat(response.getDisponivel()).isFalse();
    }

    @Test
    void deveLancarBusinessExceptionAoAtualizarLivroComIsbnDeOutroRegistro() {
        Livro livroAtual = criarLivro(1L, "ISBN-001");
        Livro outroLivro = criarLivro(2L, "ISBN-002");
        LivroRequestDTO request = new LivroRequestDTO("Livro", "Autor", "isbn-002");

        when(livroRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(livroAtual));
        when(livroRepository.findByIsbnIgnoreCase("ISBN-002")).thenReturn(Optional.of(outroLivro));

        assertThatThrownBy(() -> livroService.atualizarLivro(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O ISBN informado pertence a outro livro.");
    }

    @Test
    void deveExcluirLivroQuandoNaoPossuiEmprestimos() {
        Livro livro = criarLivro(1L, "ISBN-001");

        when(livroRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(livro));
        when(emprestimoRepository.existsByLivro_Id(1L)).thenReturn(false);

        livroService.excluirLivro(1L);

        verify(livroRepository).delete(livro);
    }

    @Test
    void deveImpedirExclusaoDeLivroComEmprestimos() {
        Livro livro = criarLivro(1L, "ISBN-001");

        when(livroRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(livro));
        when(emprestimoRepository.existsByLivro_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> livroService.excluirLivro(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Nao e possivel excluir um livro com emprestimos vinculados.");
    }

    private Livro criarLivro(Long id, String isbn) {
        Livro livro = new Livro();
        livro.setId(id);
        livro.setTitulo("Livro " + id);
        livro.setAutor("Autor " + id);
        livro.setIsbn(isbn);
        livro.setDisponivel(true);
        return livro;
    }
}
