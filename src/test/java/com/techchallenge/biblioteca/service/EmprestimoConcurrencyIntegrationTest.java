package com.techchallenge.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.techchallenge.biblioteca.dto.EmprestimoRequestDTO;
import com.techchallenge.biblioteca.dto.LivroRequestDTO;
import com.techchallenge.biblioteca.dto.LivroResponseDTO;
import com.techchallenge.biblioteca.dto.UsuarioRequestDTO;
import com.techchallenge.biblioteca.exception.BusinessException;
import com.techchallenge.biblioteca.repository.EmprestimoRepository;
import com.techchallenge.biblioteca.repository.LivroRepository;
import com.techchallenge.biblioteca.repository.UsuarioRepository;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EmprestimoConcurrencyIntegrationTest {

    @Autowired
    private LivroService livroService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmprestimoService emprestimoService;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @AfterEach
    void limparDados() {
        emprestimoRepository.deleteAll();
        livroRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void devePermitirApenasUmEmprestimoConcorrenteParaOMesmoLivro() throws Exception {
        LivroResponseDTO livro = livroService.cadastrarLivro(
                new LivroRequestDTO(" Clean Architecture ", " Robert C. Martin ", " isbn-777 ")
        );
        Long primeiroUsuarioId = usuarioService.cadastrarUsuario(
                new UsuarioRequestDTO(" Maria ", " maria.concorrencia@mail.com ")
        ).getId();
        Long segundoUsuarioId = usuarioService.cadastrarUsuario(
                new UsuarioRequestDTO(" Joao ", " joao.concorrencia@mail.com ")
        ).getId();

        CountDownLatch inicioSimultaneo = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> primeiroResultado = executor.submit(
                    executarEmprestimo(inicioSimultaneo, livro.getId(), primeiroUsuarioId)
            );
            Future<Boolean> segundoResultado = executor.submit(
                    executarEmprestimo(inicioSimultaneo, livro.getId(), segundoUsuarioId)
            );

            inicioSimultaneo.countDown();

            boolean primeiroSucesso = primeiroResultado.get(10, TimeUnit.SECONDS);
            boolean segundoSucesso = segundoResultado.get(10, TimeUnit.SECONDS);

            assertThat(List.of(primeiroSucesso, segundoSucesso)).containsExactlyInAnyOrder(true, false);
            assertThat(emprestimoRepository.findEmprestimosAtivosByLivroId(livro.getId())).hasSize(1);
            assertThat(livroRepository.findById(livro.getId()).orElseThrow().isDisponivel()).isFalse();
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<Boolean> executarEmprestimo(CountDownLatch inicioSimultaneo, Long livroId, Long usuarioId) {
        return () -> {
            inicioSimultaneo.await(10, TimeUnit.SECONDS);

            try {
                emprestimoService.realizarEmprestimo(new EmprestimoRequestDTO(livroId, usuarioId, 7));
                return true;
            } catch (BusinessException exception) {
                return false;
            }
        };
    }
}
