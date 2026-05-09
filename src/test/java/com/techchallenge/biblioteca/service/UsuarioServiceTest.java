package com.techchallenge.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techchallenge.biblioteca.dto.UsuarioRequestDTO;
import com.techchallenge.biblioteca.dto.UsuarioResponseDTO;
import com.techchallenge.biblioteca.entity.Usuario;
import com.techchallenge.biblioteca.exception.BusinessException;
import com.techchallenge.biblioteca.exception.ResourceNotFoundException;
import com.techchallenge.biblioteca.repository.EmprestimoRepository;
import com.techchallenge.biblioteca.repository.UsuarioRepository;
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
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarUsuarioComEmailNormalizado() {
        UsuarioRequestDTO request = new UsuarioRequestDTO(" Maria Silva ", " MARIA@MAIL.COM ");

        when(usuarioRepository.existsByEmailIgnoreCase("maria@mail.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioResponseDTO response = usuarioService.cadastrarUsuario(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("Maria Silva");
        assertThat(response.getEmail()).isEqualTo("maria@mail.com");
    }

    @Test
    void deveLancarBusinessExceptionAoCadastrarUsuarioComEmailDuplicado() {
        UsuarioRequestDTO request = new UsuarioRequestDTO("Maria", "maria@mail.com");

        when(usuarioRepository.existsByEmailIgnoreCase("maria@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrarUsuario(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ja existe um usuario cadastrado com o e-mail informado.");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void deveListarUsuariosPaginados() {
        Usuario usuario = criarUsuario(1L, "maria@mail.com");
        PageRequest pageable = PageRequest.of(0, 10);

        when(usuarioRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(usuario), pageable, 1));

        Page<UsuarioResponseDTO> response = usuarioService.listarUsuarios(pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getNome()).isEqualTo("Usuario 1");
    }

    @Test
    void deveBuscarUsuarioPorId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(criarUsuario(1L, "maria@mail.com")));

        UsuarioResponseDTO response = usuarioService.buscarUsuarioPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("maria@mail.com");
    }

    @Test
    void deveLancarNotFoundAoBuscarUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarUsuarioPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuario com id 99 nao encontrado.");
    }

    @Test
    void deveAtualizarUsuarioQuandoEmailPertenceAoMesmoRegistro() {
        Usuario usuario = criarUsuario(1L, "maria@mail.com");
        UsuarioRequestDTO request = new UsuarioRequestDTO(" Maria Souza ", " MARIA@MAIL.COM ");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmailIgnoreCase("maria@mail.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponseDTO response = usuarioService.atualizarUsuario(1L, request);

        assertThat(response.getNome()).isEqualTo("Maria Souza");
        assertThat(response.getEmail()).isEqualTo("maria@mail.com");
    }

    @Test
    void deveLancarBusinessExceptionAoAtualizarUsuarioComEmailDeOutroRegistro() {
        Usuario usuarioAtual = criarUsuario(1L, "maria@mail.com");
        Usuario outroUsuario = criarUsuario(2L, "joao@mail.com");
        UsuarioRequestDTO request = new UsuarioRequestDTO("Maria", "joao@mail.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAtual));
        when(usuarioRepository.findByEmailIgnoreCase("joao@mail.com")).thenReturn(Optional.of(outroUsuario));

        assertThatThrownBy(() -> usuarioService.atualizarUsuario(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O e-mail informado pertence a outro usuario.");
    }

    @Test
    void deveExcluirUsuarioQuandoNaoPossuiEmprestimos() {
        Usuario usuario = criarUsuario(1L, "maria@mail.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(emprestimoRepository.existsByUsuario_Id(1L)).thenReturn(false);

        usuarioService.excluirUsuario(1L);

        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void deveImpedirExclusaoDeUsuarioComEmprestimos() {
        Usuario usuario = criarUsuario(1L, "maria@mail.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(emprestimoRepository.existsByUsuario_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.excluirUsuario(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Nao e possivel excluir um usuario com emprestimos vinculados.");
    }

    private Usuario criarUsuario(Long id, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuario " + id);
        usuario.setEmail(email);
        return usuario;
    }
}
