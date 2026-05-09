package com.techchallenge.biblioteca.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioLivroMaisEmprestadoDTO {

    private Long livroId;
    private String titulo;
    private String autor;
    private String isbn;
    private Long quantidadeEmprestimos;
}
