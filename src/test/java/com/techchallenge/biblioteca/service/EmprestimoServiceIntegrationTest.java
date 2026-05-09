package com.techchallenge.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.techchallenge.biblioteca.dto.DevolucaoRequestDTO;
import com.techchallenge.biblioteca.dto.EmprestimoRequestDTO;
import com.techchallenge.biblioteca.dto.EmprestimoResponseDTO;
import com.techchallenge.biblioteca.dto.LivroRequestDTO;
import com.techchallenge.biblioteca.dto.LivroResponseDTO;
import com.techchallenge.biblioteca.dto.UsuarioRequestDTO;
import com.techchallenge.biblioteca.entity.Emprestimo;
import com.techchallenge.biblioteca.entity.Livro;
import com.techchallenge.biblioteca.enums.StatusEmprestimo;
import com.techchallenge.biblioteca.repository.EmprestimoRepository;
import com.techchallenge.biblioteca.repository.LivroRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EmprestimoServiceIntegrationTest {

    @Autowired
    private LivroService livroService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmprestimoService emprestimoService;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Test
    void devePersistirFluxoCompletoDeEmprestimoEDevolucao() {
        LivroResponseDTO livro = livroService.cadastrarLivro(
                new LivroRequestDTO(" Domain-Driven Design ", " Eric Evans ", " isbn-123 ")
        );
        Long usuarioId = usuarioService.cadastrarUsuario(
                new UsuarioRequestDTO(" Ana ", " ANA@MAIL.COM ")
        ).getId();

        EmprestimoResponseDTO emprestimo = emprestimoService.realizarEmprestimo(
                new EmprestimoRequestDTO(livro.getId(), usuarioId, 14)
        );

        Livro livroEmprestado = livroRepository.findById(livro.getId()).orElseThrow();
        List<Emprestimo> emprestimosAtivos = emprestimoRepository.findEmprestimosAtivosByLivroId(livro.getId());

        assertThat(emprestimo.getStatus()).isEqualTo(StatusEmprestimo.EMPRESTADO);
        assertThat(livroEmprestado.isDisponivel()).isFalse();
        assertThat(emprestimosAtivos).hasSize(1);

        EmprestimoResponseDTO devolucao = emprestimoService.registrarDevolucao(
                new DevolucaoRequestDTO(emprestimo.getId())
        );

        Livro livroDevolvido = livroRepository.findById(livro.getId()).orElseThrow();

        assertThat(devolucao.getStatus()).isEqualTo(StatusEmprestimo.DEVOLVIDO);
        assertThat(devolucao.getDataDevolucao()).isNotNull();
        assertThat(livroDevolvido.isDisponivel()).isTrue();
    }
}
