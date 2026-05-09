package com.techchallenge.biblioteca.dto;

import com.techchallenge.biblioteca.enums.StatusEmprestimo;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmprestimoResponseDTO {

    private Long id;
    private Long livroId;
    private String tituloLivro;
    private Long usuarioId;
    private String nomeUsuario;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevistaDevolucao;
    private LocalDate dataDevolucao;
    private StatusEmprestimo status;
}
