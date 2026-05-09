package com.techchallenge.biblioteca.mapper;

import com.techchallenge.biblioteca.dto.LivroRequestDTO;
import com.techchallenge.biblioteca.dto.LivroResponseDTO;
import com.techchallenge.biblioteca.entity.Livro;

public final class LivroMapper {

    private LivroMapper() {
    }

    public static LivroResponseDTO toResponseDTO(Livro livro) {
        if (livro == null) {
            return null;
        }

        return new LivroResponseDTO(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor(),
                livro.getIsbn(),
                livro.isDisponivel()
        );
    }

    public static Livro toEntity(LivroRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }

        Livro livro = new Livro();
        updateEntity(livro, requestDTO);
        livro.setDisponivel(true);
        return livro;
    }

    public static Livro updateEntity(Livro livro, LivroRequestDTO requestDTO) {
        if (livro == null) {
            return null;
        }

        if (requestDTO == null) {
            return livro;
        }

        livro.setTitulo(requestDTO.getTitulo());
        livro.setAutor(requestDTO.getAutor());
        livro.setIsbn(requestDTO.getIsbn());
        return livro;
    }
}
