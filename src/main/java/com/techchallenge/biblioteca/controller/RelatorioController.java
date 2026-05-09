package com.techchallenge.biblioteca.controller;

import com.techchallenge.biblioteca.dto.RelatorioLivroEmprestadoDTO;
import com.techchallenge.biblioteca.dto.RelatorioLivroMaisEmprestadoDTO;
import com.techchallenge.biblioteca.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatorios", description = "Consultas consolidadas para apoio operacional e gerencial.")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/livros-mais-emprestados")
    @Operation(summary = "Listar os 20 livros mais emprestados")
    @ApiResponse(responseCode = "200", description = "Relatorio gerado com sucesso")
    public ResponseEntity<List<RelatorioLivroMaisEmprestadoDTO>> listarLivrosMaisEmprestados() {
        List<RelatorioLivroMaisEmprestadoDTO> response = relatorioService.listarLivrosMaisEmprestados();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/livros-emprestados")
    @Operation(summary = "Listar livros emprestados no momento com previsao de devolucao")
    @ApiResponse(responseCode = "200", description = "Relatorio gerado com sucesso")
    public ResponseEntity<List<RelatorioLivroEmprestadoDTO>> listarLivrosEmprestadosNoMomento() {
        List<RelatorioLivroEmprestadoDTO> response = relatorioService.listarLivrosEmprestadosNoMomento();
        return ResponseEntity.ok(response);
    }
}
