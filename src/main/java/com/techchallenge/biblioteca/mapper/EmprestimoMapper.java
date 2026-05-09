package com.techchallenge.biblioteca.mapper;

import com.techchallenge.biblioteca.dto.EmprestimoResponseDTO;
import com.techchallenge.biblioteca.dto.RelatorioLivroEmprestadoDTO;
import com.techchallenge.biblioteca.dto.RelatorioLivroMaisEmprestadoDTO;
import com.techchallenge.biblioteca.entity.Emprestimo;
import com.techchallenge.biblioteca.entity.Livro;
import com.techchallenge.biblioteca.entity.Usuario;

public final class EmprestimoMapper {

    private EmprestimoMapper() {
    }

    public static EmprestimoResponseDTO toResponseDTO(Emprestimo emprestimo) {
        if (emprestimo == null) {
            return null;
        }

        Livro livro = emprestimo.getLivro();
        Usuario usuario = emprestimo.getUsuario();

        return new EmprestimoResponseDTO(
                emprestimo.getId(),
                livro != null ? livro.getId() : null,
                livro != null ? livro.getTitulo() : null,
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getNome() : null,
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataPrevistaDevolucao(),
                emprestimo.getDataDevolucao(),
                emprestimo.getStatus()
        );
    }

    public static RelatorioLivroEmprestadoDTO toRelatorioLivroEmprestadoDTO(Emprestimo emprestimo) {
        if (emprestimo == null) {
            return null;
        }

        Livro livro = emprestimo.getLivro();
        Usuario usuario = emprestimo.getUsuario();

        return new RelatorioLivroEmprestadoDTO(
                emprestimo.getId(),
                livro != null ? livro.getId() : null,
                livro != null ? livro.getTitulo() : null,
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getNome() : null,
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataPrevistaDevolucao()
        );
    }

    public static RelatorioLivroMaisEmprestadoDTO toRelatorioLivroMaisEmprestadoDTO(Object[] resultado) {
        if (resultado == null) {
            return null;
        }

        if (resultado.length < 5) {
            throw new IllegalArgumentException("Resultado agregado invalido para o relatorio de livros mais emprestados.");
        }

        return new RelatorioLivroMaisEmprestadoDTO(
                toLong(resultado[0]),
                toStringValue(resultado[1]),
                toStringValue(resultado[2]),
                toStringValue(resultado[3]),
                toLong(resultado[4])
        );
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalArgumentException("Valor numerico invalido no resultado agregado.");
    }

    private static String toStringValue(Object value) {
        return value != null ? value.toString() : null;
    }
}
