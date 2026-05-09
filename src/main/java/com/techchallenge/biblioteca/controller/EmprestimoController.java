package com.techchallenge.biblioteca.controller;

import com.techchallenge.biblioteca.dto.DevolucaoRequestDTO;
import com.techchallenge.biblioteca.dto.EmprestimoRequestDTO;
import com.techchallenge.biblioteca.dto.EmprestimoResponseDTO;
import com.techchallenge.biblioteca.enums.StatusEmprestimo;
import com.techchallenge.biblioteca.service.EmprestimoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emprestimos")
@RequiredArgsConstructor
@Tag(name = "Emprestimos", description = "Operacoes de emprestimo, devolucao e consulta de emprestimos.")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    @PostMapping
    @Operation(summary = "Realizar emprestimo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Emprestimo realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Livro indisponivel ou dados invalidos", content = @Content(schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "404", description = "Livro ou usuario nao encontrado", content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<EmprestimoResponseDTO> realizarEmprestimo(
            @Valid @RequestBody EmprestimoRequestDTO request
    ) {
        EmprestimoResponseDTO response = emprestimoService.realizarEmprestimo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/devolucao")
    @Operation(summary = "Registrar devolucao")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolucao registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Emprestimo nao esta ativo ou corpo invalido", content = @Content(schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "404", description = "Emprestimo nao encontrado", content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<EmprestimoResponseDTO> registrarDevolucao(
            @Valid @RequestBody DevolucaoRequestDTO request
    ) {
        EmprestimoResponseDTO response = emprestimoService.registrarDevolucao(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar emprestimos por status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status invalido", content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<Page<EmprestimoResponseDTO>> listarEmprestimosPorStatus(
            @Parameter(description = "Status do emprestimo") @RequestParam StatusEmprestimo status,
            @ParameterObject
            @PageableDefault(size = 20, sort = "dataEmprestimo", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EmprestimoResponseDTO> response = emprestimoService.listarEmprestimosPorStatus(status, pageable);
        return ResponseEntity.ok(response);
    }
}
