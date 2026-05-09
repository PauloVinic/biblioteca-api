package com.techchallenge.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LivroRequestDTO {

    @NotBlank
    @Size(max = 255)
    private String titulo;

    @NotBlank
    @Size(max = 255)
    private String autor;

    @NotBlank
    @Size(max = 20)
    private String isbn;
}
