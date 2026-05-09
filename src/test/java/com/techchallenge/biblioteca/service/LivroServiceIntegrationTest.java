package com.techchallenge.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.techchallenge.biblioteca.dto.EmprestimoRequestDTO;
import com.techchallenge.biblioteca.dto.LivroRequestDTO;
import com.techchallenge.biblioteca.dto.LivroResponseDTO;
import com.techchallenge.biblioteca.dto.UsuarioRequestDTO;
import com.techchallenge.biblioteca.entity.Livro;
import com.techchallenge.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LivroServiceIntegrationTest {

    @Autowired
    private LivroService livroService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmprestimoService emprestimoService;

    @Autowired
    private LivroRepository livroRepository;

    @Test
    void deveManterLivroIndisponivelAoAtualizarDadosComEmprestimoAtivo() {
        LivroResponseDTO livro = livroService.cadastrarLivro(
                new LivroRequestDTO(" Clean Code ", " Robert C. Martin ", " isbn-001 ")
        );
        Long usuarioId = usuarioService.cadastrarUsuario(
                new UsuarioRequestDTO(" Maria ", " MARIA@MAIL.COM ")
        ).getId();

        emprestimoService.realizarEmprestimo(new EmprestimoRequestDTO(livro.getId(), usuarioId, 7));

        LivroResponseDTO response = livroService.atualizarLivro(
                livro.getId(),
                new LivroRequestDTO(" Refactoring ", " Martin Fowler ", " isbn-999 ")
        );

        Livro livroPersistido = livroRepository.findById(livro.getId()).orElseThrow();

        assertThat(response.getTitulo()).isEqualTo("Refactoring");
        assertThat(response.getAutor()).isEqualTo("Martin Fowler");
        assertThat(response.getIsbn()).isEqualTo("ISBN-999");
        assertThat(response.getDisponivel()).isFalse();
        assertThat(livroPersistido.isDisponivel()).isFalse();
    }
}
