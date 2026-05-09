package com.techchallenge.biblioteca.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LivroResponseDTO {

    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private Boolean disponivel;
}
