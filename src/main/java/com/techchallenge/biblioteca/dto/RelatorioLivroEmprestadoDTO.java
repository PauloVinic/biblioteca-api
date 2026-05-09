package com.techchallenge.biblioteca.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioLivroEmprestadoDTO {

    private Long emprestimoId;
    private Long livroId;
    private String titulo;
    private Long usuarioId;
    private String nomeUsuario;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevistaDevolucao;
}
