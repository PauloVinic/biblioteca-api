package com.techchallenge.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techchallenge.biblioteca.dto.DevolucaoRequestDTO;
import com.techchallenge.biblioteca.dto.EmprestimoRequestDTO;
import com.techchallenge.biblioteca.dto.EmprestimoResponseDTO;
import com.techchallenge.biblioteca.entity.Emprestimo;
import com.techchallenge.biblioteca.entity.Livro;
import com.techchallenge.biblioteca.entity.Usuario;
import com.techchallenge.biblioteca.enums.StatusEmprestimo;
import com.techchallenge.biblioteca.exception.BusinessException;
import com.techchallenge.biblioteca.exception.ResourceNotFoundException;
import com.techchallenge.biblioteca.repository.EmprestimoRepository;
import com.techchallenge.biblioteca.repository.LivroRepository;
import com.techchallenge.biblioteca.repository.UsuarioRepository;
import java.time.LocalDate;
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
class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EmprestimoService emprestimoService;

    @Test
    void deveRealizarEmprestimoQuandoLivroDisponivel() {
        Livro livro = criarLivro(1L, true);
        Usuario usuario = criarUsuario(2L);
        EmprestimoRequestDTO request = new EmprestimoRequestDTO(1L, 2L, 7);
        LocalDate hoje = LocalDate.now();

        when(livroRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(livro));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocation -> {
            Emprestimo emprestimo = invocation.getArgument(0);
            emprestimo.setId(10L);
            return emprestimo;
        });
        when(livroRepository.save(any(Livro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmprestimoResponseDTO response = emprestimoService.realizarEmprestimo(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getLivroId()).isEqualTo(1L);
        assertThat(response.getUsuarioId()).isEqualTo(2L);
        assertThat(response.getStatus()).isEqualTo(StatusEmprestimo.EMPRESTADO);
        assertThat(response.getDataEmprestimo()).isEqualTo(hoje);
        assertThat(response.getDataPrevistaDevolucao()).isEqualTo(hoje.plusDays(7));
        assertThat(livro.isDisponivel()).isFalse();
    }

    @Test
    void deveLancarNotFoundQuandoLivroNaoExistirNoEmprestimo() {
        EmprestimoRequestDTO request = new EmprestimoRequestDTO(1L, 2L, 7);

        when(livroRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emprestimoService.realizarEmprestimo(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Livro com id 1 nao encontrado.");
    }

    @Test
    void deveLancarNotFoundQuandoUsuarioNaoExistirNoEmprestimo() {
        Livro livro = criarLivro(1L, true);
        EmprestimoRequestDTO request = new EmprestimoRequestDTO(1L, 2L, 7);

        when(livroRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(livro));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emprestimoService.realizarEmprestimo(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuario com id 2 nao encontrado.");
    }

    @Test
    void deveLancarBusinessExceptionQuandoLivroIndisponivelNoEmprestimo() {
        Livro livro = criarLivro(1L, false);
        Usuario usuario = criarUsuario(2L);
        EmprestimoRequestDTO request = new EmprestimoRequestDTO(1L, 2L, 7);

        when(livroRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(livro));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> emprestimoService.realizarEmprestimo(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O livro informado nao esta disponivel para emprestimo.");
    }

    @Test
    void deveLancarBusinessExceptionQuandoLivroJaPossuiEmprestimoAtivo() {
        Livro livro = criarLivro(1L, true);
        Usuario usuario = criarUsuario(2L);
        EmprestimoRequestDTO request = new EmprestimoRequestDTO(1L, 2L, 7);

        when(livroRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(livro));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(emprestimoRepository.existsByLivro_IdAndStatus(1L, StatusEmprestimo.EMPRESTADO)).thenReturn(true);

        assertThatThrownBy(() -> emprestimoService.realizarEmprestimo(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O livro informado ja possui um emprestimo ativo.");
    }

    @Test
    void deveRegistrarDevolucaoQuandoEmprestimoAtivo() {
        Livro livro = criarLivro(1L, false);
        Usuario usuario = criarUsuario(2L);
        Emprestimo emprestimo = criarEmprestimo(10L, livro, usuario, StatusEmprestimo.EMPRESTADO);
        LocalDate hoje = LocalDate.now();

        when(emprestimoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(livroRepository.save(any(Livro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmprestimoResponseDTO response = emprestimoService.registrarDevolucao(new DevolucaoRequestDTO(10L));

        assertThat(response.getStatus()).isEqualTo(StatusEmprestimo.DEVOLVIDO);
        assertThat(response.getDataDevolucao()).isEqualTo(hoje);
        assertThat(livro.isDisponivel()).isTrue();
    }

    @Test
    void deveManterLivroIndisponivelQuandoExisteOutroEmprestimoAtivoNaDevolucao() {
        Livro livro = criarLivro(1L, false);
        Usuario usuario = criarUsuario(2L);
        Emprestimo emprestimo = criarEmprestimo(10L, livro, usuario, StatusEmprestimo.EMPRESTADO);

        when(emprestimoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.existsByLivro_IdAndStatusAndIdNot(1L, StatusEmprestimo.EMPRESTADO, 10L))
                .thenReturn(true);
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(livroRepository.save(any(Livro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmprestimoResponseDTO response = emprestimoService.registrarDevolucao(new DevolucaoRequestDTO(10L));

        assertThat(response.getStatus()).isEqualTo(StatusEmprestimo.DEVOLVIDO);
        assertThat(livro.isDisponivel()).isFalse();
    }

    @Test
    void deveLancarNotFoundQuandoEmprestimoNaoExistirNaDevolucao() {
        when(emprestimoRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emprestimoService.registrarDevolucao(new DevolucaoRequestDTO(10L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Emprestimo com id 10 nao encontrado.");
    }

    @Test
    void deveLancarBusinessExceptionQuandoEmprestimoNaoEstiverAtivoNaDevolucao() {
        Livro livro = criarLivro(1L, false);
        Usuario usuario = criarUsuario(2L);
        Emprestimo emprestimo = criarEmprestimo(10L, livro, usuario, StatusEmprestimo.DEVOLVIDO);

        when(emprestimoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(emprestimo));

        assertThatThrownBy(() -> emprestimoService.registrarDevolucao(new DevolucaoRequestDTO(10L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Somente emprestimos com status EMPRESTADO podem ser devolvidos.");
    }

    @Test
    void deveListarEmprestimosPorStatus() {
        Livro livro = criarLivro(1L, false);
        Usuario usuario = criarUsuario(2L);
        Emprestimo emprestimo = criarEmprestimo(10L, livro, usuario, StatusEmprestimo.EMPRESTADO);
        PageRequest pageable = PageRequest.of(0, 10);

        when(emprestimoRepository.findByStatus(StatusEmprestimo.EMPRESTADO, pageable))
                .thenReturn(new PageImpl<>(List.of(emprestimo), pageable, 1));

        Page<EmprestimoResponseDTO> response =
                emprestimoService.listarEmprestimosPorStatus(StatusEmprestimo.EMPRESTADO, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTituloLivro()).isEqualTo("Livro 1");
    }

    private Livro criarLivro(Long id, boolean disponivel) {
        Livro livro = new Livro();
        livro.setId(id);
        livro.setTitulo("Livro " + id);
        livro.setAutor("Autor " + id);
        livro.setIsbn("ISBN-" + id);
        livro.setDisponivel(disponivel);
        return livro;
    }

    private Usuario criarUsuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuario " + id);
        usuario.setEmail("usuario" + id + "@mail.com");
        return usuario;
    }

    private Emprestimo criarEmprestimo(Long id, Livro livro, Usuario usuario, StatusEmprestimo status) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(id);
        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(LocalDate.now().minusDays(2));
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(5));
        emprestimo.setStatus(status);
        return emprestimo;
    }
}
