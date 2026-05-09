package com.techchallenge.biblioteca.service;

import com.techchallenge.biblioteca.dto.DevolucaoRequestDTO;
import com.techchallenge.biblioteca.dto.EmprestimoRequestDTO;
import com.techchallenge.biblioteca.dto.EmprestimoResponseDTO;
import com.techchallenge.biblioteca.entity.Emprestimo;
import com.techchallenge.biblioteca.entity.Livro;
import com.techchallenge.biblioteca.entity.Usuario;
import com.techchallenge.biblioteca.enums.StatusEmprestimo;
import com.techchallenge.biblioteca.exception.BusinessException;
import com.techchallenge.biblioteca.exception.ResourceNotFoundException;
import com.techchallenge.biblioteca.mapper.EmprestimoMapper;
import com.techchallenge.biblioteca.repository.EmprestimoRepository;
import com.techchallenge.biblioteca.repository.LivroRepository;
import com.techchallenge.biblioteca.repository.UsuarioRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public EmprestimoResponseDTO realizarEmprestimo(EmprestimoRequestDTO request) {
        Livro livro = buscarLivroPorIdParaEmprestimo(request.getLivroId());
        Usuario usuario = buscarUsuarioPorId(request.getUsuarioId());
        validarLivroDisponivel(livro);
        validarLivroSemEmprestimoAtivo(livro.getId());

        LocalDate dataAtual = LocalDate.now();

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(dataAtual);
        emprestimo.setDataPrevistaDevolucao(dataAtual.plusDays(request.getQuantidadeDiasEmprestimo()));
        emprestimo.setDataDevolucao(null);
        emprestimo.setStatus(StatusEmprestimo.EMPRESTADO);

        livro.setDisponivel(false);

        Emprestimo emprestimoSalvo = emprestimoRepository.save(emprestimo);
        livroRepository.save(livro);

        return EmprestimoMapper.toResponseDTO(emprestimoSalvo);
    }

    @Transactional
    public EmprestimoResponseDTO registrarDevolucao(DevolucaoRequestDTO request) {
        Emprestimo emprestimo = buscarEmprestimoPorIdParaDevolucao(request.getEmprestimoId());
        validarEmprestimoAtivo(emprestimo);

        Livro livro = emprestimo.getLivro();
        boolean possuiOutroEmprestimoAtivo = emprestimoRepository.existsByLivro_IdAndStatusAndIdNot(
                livro.getId(),
                StatusEmprestimo.EMPRESTADO,
                emprestimo.getId()
        );

        emprestimo.setDataDevolucao(LocalDate.now());
        emprestimo.setStatus(StatusEmprestimo.DEVOLVIDO);
        livro.setDisponivel(!possuiOutroEmprestimoAtivo);

        Emprestimo emprestimoAtualizado = emprestimoRepository.save(emprestimo);
        livroRepository.save(livro);

        return EmprestimoMapper.toResponseDTO(emprestimoAtualizado);
    }

    @Transactional(readOnly = true)
    public Page<EmprestimoResponseDTO> listarEmprestimosPorStatus(StatusEmprestimo status, Pageable pageable) {
        return emprestimoRepository.findByStatus(status, pageable)
                .map(EmprestimoMapper::toResponseDTO);
    }

    private Livro buscarLivroPorIdParaEmprestimo(Long id) {
        return livroRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro com id " + id + " nao encontrado."));
    }

    private Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com id " + id + " nao encontrado."));
    }

    private Emprestimo buscarEmprestimoPorIdParaDevolucao(Long id) {
        return emprestimoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emprestimo com id " + id + " nao encontrado."));
    }

    private void validarLivroDisponivel(Livro livro) {
        if (!livro.isDisponivel()) {
            throw new BusinessException("O livro informado nao esta disponivel para emprestimo.");
        }
    }

    private void validarLivroSemEmprestimoAtivo(Long livroId) {
        if (emprestimoRepository.existsByLivro_IdAndStatus(livroId, StatusEmprestimo.EMPRESTADO)) {
            throw new BusinessException("O livro informado ja possui um emprestimo ativo.");
        }
    }

    private void validarEmprestimoAtivo(Emprestimo emprestimo) {
        if (emprestimo.getStatus() != StatusEmprestimo.EMPRESTADO) {
            throw new BusinessException("Somente emprestimos com status EMPRESTADO podem ser devolvidos.");
        }
    }
}
