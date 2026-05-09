package com.techchallenge.biblioteca.controller;

import com.techchallenge.biblioteca.dto.LivroRequestDTO;
import com.techchallenge.biblioteca.dto.LivroResponseDTO;
import com.techchallenge.biblioteca.service.LivroService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/livros")
@RequiredArgsConstructor
@Tag(name = "Livros", description = "Operacoes de cadastro, consulta, atualizacao e exclusao de livros.")
public class LivroController {

    private final LivroService livroService;

    @PostMapping
    @Operation(summary = "Cadastrar livro")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Livro cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou ISBN duplicado", content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<LivroResponseDTO> cadastrarLivro(@Valid @RequestBody LivroRequestDTO request) {
        LivroResponseDTO response = livroService.cadastrarLivro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar livros", description = "Lista livros com filtros opcionais de titulo, autor e ISBN.")
    @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso")
    public ResponseEntity<Page<LivroResponseDTO>> listarLivros(
            @Parameter(description = "Filtro por titulo") @RequestParam(required = false) String titulo,
            @Parameter(description = "Filtro por autor") @RequestParam(required = false) String autor,
            @Parameter(description = "Filtro por ISBN") @RequestParam(required = false) String isbn,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        Page<LivroResponseDTO> response = livroService.listarLivros(titulo, autor, isbn, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar livro por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro encontrado"),
            @ApiResponse(responseCode = "404", description = "Livro nao encontrado", content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<LivroResponseDTO> buscarLivroPorId(
            @Parameter(description = "Identificador do livro") @PathVariable Long id
    ) {
        LivroResponseDTO response = livroService.buscarLivroPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar livro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou ISBN em conflito", content = @Content(schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "404", description = "Livro nao encontrado", content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<LivroResponseDTO> atualizarLivro(
            @Parameter(description = "Identificador do livro") @PathVariable Long id,
            @Valid @RequestBody LivroRequestDTO request
    ) {
        LivroResponseDTO response = livroService.atualizarLivro(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir livro")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Livro excluido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Livro possui emprestimos vinculados", content = @Content(schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "404", description = "Livro nao encontrado", content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<Void> excluirLivro(
            @Parameter(description = "Identificador do livro") @PathVariable Long id
    ) {
        livroService.excluirLivro(id);
        return ResponseEntity.noContent().build();
    }
}
