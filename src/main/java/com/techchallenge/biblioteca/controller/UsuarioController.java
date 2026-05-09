package com.techchallenge.biblioteca.controller;

import com.techchallenge.biblioteca.dto.UsuarioRequestDTO;
import com.techchallenge.biblioteca.dto.UsuarioResponseDTO;
import com.techchallenge.biblioteca.service.UsuarioService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operacoes de cadastro, consulta, atualizacao e exclusao de usuarios.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Cadastrar usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou e-mail duplicado", content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> cadastrarUsuario(@Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.cadastrarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar usuarios")
    @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso")
    public ResponseEntity<Page<UsuarioResponseDTO>> listarUsuarios(
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        Page<UsuarioResponseDTO> response = usuarioService.listarUsuarios(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioPorId(
            @Parameter(description = "Identificador do usuario") @PathVariable Long id
    ) {
        UsuarioResponseDTO response = usuarioService.buscarUsuarioPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou e-mail em conflito", content = @Content(schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(
            @Parameter(description = "Identificador do usuario") @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO request
    ) {
        UsuarioResponseDTO response = usuarioService.atualizarUsuario(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario excluido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Usuario possui emprestimos vinculados", content = @Content(schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<Void> excluirUsuario(
            @Parameter(description = "Identificador do usuario") @PathVariable Long id
    ) {
        usuarioService.excluirUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
