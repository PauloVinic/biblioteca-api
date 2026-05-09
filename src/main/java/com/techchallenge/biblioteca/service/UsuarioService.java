package com.techchallenge.biblioteca.service;

import com.techchallenge.biblioteca.dto.UsuarioRequestDTO;
import com.techchallenge.biblioteca.dto.UsuarioResponseDTO;
import com.techchallenge.biblioteca.entity.Usuario;
import com.techchallenge.biblioteca.exception.BusinessException;
import com.techchallenge.biblioteca.exception.ResourceNotFoundException;
import com.techchallenge.biblioteca.mapper.UsuarioMapper;
import com.techchallenge.biblioteca.repository.EmprestimoRepository;
import com.techchallenge.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;

    @Transactional
    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO request) {
        UsuarioRequestDTO requestNormalizado = normalizarRequest(request);
        validarEmailDuplicado(requestNormalizado.getEmail());

        Usuario usuario = UsuarioMapper.toEntity(requestNormalizado);
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponseDTO(usuarioSalvo);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(UsuarioMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioPorId(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        return UsuarioMapper.toResponseDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuario(Long id, UsuarioRequestDTO request) {
        Usuario usuario = buscarEntidadePorId(id);
        UsuarioRequestDTO requestNormalizado = normalizarRequest(request);
        validarEmailDuplicadoParaOutroUsuario(requestNormalizado.getEmail(), id);

        UsuarioMapper.updateEntity(usuario, requestNormalizado);
        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponseDTO(usuarioAtualizado);
    }

    @Transactional
    public void excluirUsuario(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        validarUsuarioSemEmprestimosVinculados(id);
        usuarioRepository.delete(usuario);
    }

    private Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com id " + id + " nao encontrado."));
    }

    private void validarEmailDuplicado(String email) {
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Ja existe um usuario cadastrado com o e-mail informado.");
        }
    }

    private void validarEmailDuplicadoParaOutroUsuario(String email, Long usuarioId) {
        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(usuarioExistente -> !usuarioExistente.getId().equals(usuarioId))
                .ifPresent(usuarioExistente -> {
                    throw new BusinessException("O e-mail informado pertence a outro usuario.");
                });
    }

    private void validarUsuarioSemEmprestimosVinculados(Long usuarioId) {
        if (emprestimoRepository.existsByUsuario_Id(usuarioId)) {
            throw new BusinessException("Nao e possivel excluir um usuario com emprestimos vinculados.");
        }
    }

    private UsuarioRequestDTO normalizarRequest(UsuarioRequestDTO request) {
        return new UsuarioRequestDTO(
                StringNormalizer.normalizeText(request.getNome()),
                StringNormalizer.normalizeEmail(request.getEmail())
        );
    }
}
