package com.techchallenge.biblioteca.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmprestimoRequestDTO {

    @NotNull
    private Long livroId;

    @NotNull
    private Long usuarioId;

    @NotNull
    @Positive
    @Max(90)
    private Integer quantidadeDiasEmprestimo;
}
