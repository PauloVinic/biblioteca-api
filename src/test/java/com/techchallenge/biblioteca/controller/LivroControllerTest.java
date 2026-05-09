package com.techchallenge.biblioteca.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.biblioteca.config.OpenApiConfig;
import com.techchallenge.biblioteca.dto.LivroRequestDTO;
import com.techchallenge.biblioteca.dto.LivroResponseDTO;
import com.techchallenge.biblioteca.exception.GlobalExceptionHandler;
import com.techchallenge.biblioteca.exception.ResourceNotFoundException;
import com.techchallenge.biblioteca.service.LivroService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LivroController.class)
@Import({GlobalExceptionHandler.class, OpenApiConfig.class})
class LivroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LivroService livroService;

    private static final String LONG_TEXT = "A".repeat(256);

    @Test
    void deveCadastrarLivro() throws Exception {
        LivroRequestDTO request = new LivroRequestDTO("Clean Code", "Robert C. Martin", "ISBN-001");
        LivroResponseDTO response = new LivroResponseDTO(1L, "Clean Code", "Robert C. Martin", "ISBN-001", true);

        when(livroService.cadastrarLivro(any(LivroRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Clean Code"));
    }

    @Test
    void deveRetornarBadRequestAoCadastrarLivroInvalido() throws Exception {
        LivroRequestDTO request = new LivroRequestDTO("", "", "");

        mockMvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void deveRetornarBadRequestAoCadastrarLivroComTituloAcimaDoLimite() throws Exception {
        LivroRequestDTO request = new LivroRequestDTO(LONG_TEXT, "Autor", "ISBN-001");

        mockMvc.perform(post("/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void deveListarLivrosPaginados() throws Exception {
        LivroResponseDTO response = new LivroResponseDTO(1L, "Clean Code", "Robert C. Martin", "ISBN-001", true);

        when(livroService.listarLivros(eq("Clean"), eq("Martin"), eq("ISBN"), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/livros")
                        .param("titulo", "Clean")
                        .param("autor", "Martin")
                        .param("isbn", "ISBN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Clean Code"));
    }

    @Test
    void deveBuscarLivroPorId() throws Exception {
        when(livroService.buscarLivroPorId(1L))
                .thenReturn(new LivroResponseDTO(1L, "Clean Code", "Robert C. Martin", "ISBN-001", true));

        mockMvc.perform(get("/livros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deveRetornarNotFoundAoBuscarLivroInexistente() throws Exception {
        when(livroService.buscarLivroPorId(99L))
                .thenThrow(new ResourceNotFoundException("Livro com id 99 nao encontrado."));

        mockMvc.perform(get("/livros/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Livro com id 99 nao encontrado."));
    }

    @Test
    void deveAtualizarLivro() throws Exception {
        LivroRequestDTO request = new LivroRequestDTO("Refactoring", "Martin Fowler", "ISBN-002");
        LivroResponseDTO response = new LivroResponseDTO(1L, "Refactoring", "Martin Fowler", "ISBN-002", false);

        when(livroService.atualizarLivro(eq(1L), any(LivroRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/livros/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Refactoring"));
    }

    @Test
    void deveExcluirLivro() throws Exception {
        mockMvc.perform(delete("/livros/1"))
                .andExpect(status().isNoContent());
    }
}
