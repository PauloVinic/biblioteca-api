package com.techchallenge.biblioteca.service;

import com.techchallenge.biblioteca.dto.LivroRequestDTO;
import com.techchallenge.biblioteca.dto.LivroResponseDTO;
import com.techchallenge.biblioteca.entity.Livro;
import com.techchallenge.biblioteca.exception.BusinessException;
import com.techchallenge.biblioteca.exception.ResourceNotFoundException;
import com.techchallenge.biblioteca.mapper.LivroMapper;
import com.techchallenge.biblioteca.repository.EmprestimoRepository;
import com.techchallenge.biblioteca.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository livroRepository;
    private final EmprestimoRepository emprestimoRepository;

    @Transactional
    public LivroResponseDTO cadastrarLivro(LivroRequestDTO request) {
        LivroRequestDTO requestNormalizado = normalizarRequest(request);
        validarIsbnDuplicado(requestNormalizado.getIsbn());

        Livro livro = LivroMapper.toEntity(requestNormalizado);
        Livro livroSalvo = livroRepository.save(livro);
        return LivroMapper.toResponseDTO(livroSalvo);
    }

    @Transactional(readOnly = true)
    public Page<LivroResponseDTO> listarLivros(String titulo, String autor, String isbn, Pageable pageable) {
        return livroRepository.buscarComFiltros(titulo, autor, isbn, pageable)
                .map(LivroMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public LivroResponseDTO buscarLivroPorId(Long id) {
        Livro livro = buscarEntidadePorId(id);
        return LivroMapper.toResponseDTO(livro);
    }

    @Transactional
    public LivroResponseDTO atualizarLivro(Long id, LivroRequestDTO request) {
        Livro livro = buscarEntidadePorIdParaAtualizacao(id);
        LivroRequestDTO requestNormalizado = normalizarRequest(request);
        validarIsbnDuplicadoParaOutroLivro(requestNormalizado.getIsbn(), id);

        LivroMapper.updateEntity(livro, requestNormalizado);
        Livro livroAtualizado = livroRepository.save(livro);
        return LivroMapper.toResponseDTO(livroAtualizado);
    }

    @Transactional
    public void excluirLivro(Long id) {
        Livro livro = buscarEntidadePorIdParaAtualizacao(id);
        validarLivroSemEmprestimosVinculados(id);
        livroRepository.delete(livro);
    }

    private Livro buscarEntidadePorId(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro com id " + id + " nao encontrado."));
    }

    private Livro buscarEntidadePorIdParaAtualizacao(Long id) {
        return livroRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro com id " + id + " nao encontrado."));
    }

    private void validarIsbnDuplicado(String isbn) {
        if (livroRepository.findByIsbnIgnoreCase(isbn).isPresent()) {
            throw new BusinessException("Ja existe um livro cadastrado com o ISBN informado.");
        }
    }

    private void validarIsbnDuplicadoParaOutroLivro(String isbn, Long livroId) {
        livroRepository.findByIsbnIgnoreCase(isbn)
                .filter(livroExistente -> !livroExistente.getId().equals(livroId))
                .ifPresent(livroExistente -> {
                    throw new BusinessException("O ISBN informado pertence a outro livro.");
                });
    }

    private void validarLivroSemEmprestimosVinculados(Long livroId) {
        if (emprestimoRepository.existsByLivro_Id(livroId)) {
            throw new BusinessException("Nao e possivel excluir um livro com emprestimos vinculados.");
        }
    }

    private LivroRequestDTO normalizarRequest(LivroRequestDTO request) {
        return new LivroRequestDTO(
                StringNormalizer.normalizeText(request.getTitulo()),
                StringNormalizer.normalizeText(request.getAutor()),
                StringNormalizer.normalizeUpperCase(request.getIsbn())
        );
    }
}
