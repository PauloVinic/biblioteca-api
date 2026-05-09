package com.techchallenge.biblioteca.service;

import com.techchallenge.biblioteca.dto.RelatorioLivroEmprestadoDTO;
import com.techchallenge.biblioteca.dto.RelatorioLivroMaisEmprestadoDTO;
import com.techchallenge.biblioteca.mapper.EmprestimoMapper;
import com.techchallenge.biblioteca.repository.EmprestimoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final EmprestimoRepository emprestimoRepository;

    @Transactional(readOnly = true)
    public List<RelatorioLivroMaisEmprestadoDTO> listarLivrosMaisEmprestados() {
        return emprestimoRepository.findLivrosMaisEmprestadosComQuantidade(PageRequest.of(0, 20))
                .stream()
                .map(EmprestimoMapper::toRelatorioLivroMaisEmprestadoDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RelatorioLivroEmprestadoDTO> listarLivrosEmprestadosNoMomento() {
        return emprestimoRepository.findEmprestimosAtivosComPrevisaoDevolucao()
                .stream()
                .map(EmprestimoMapper::toRelatorioLivroEmprestadoDTO)
                .toList();
    }
}
