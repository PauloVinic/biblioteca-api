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
import com.techchallenge.biblioteca.dto.UsuarioRequestDTO;
import com.techchallenge.biblioteca.dto.UsuarioResponseDTO;
import com.techchallenge.biblioteca.exception.GlobalExceptionHandler;
import com.techchallenge.biblioteca.exception.ResourceNotFoundException;
import com.techchallenge.biblioteca.service.UsuarioService;
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

@WebMvcTest(UsuarioController.class)
@Import({GlobalExceptionHandler.class, OpenApiConfig.class})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarUsuario() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO("Maria", "maria@mail.com");
        UsuarioResponseDTO response = new UsuarioResponseDTO(1L, "Maria", "maria@mail.com");

        when(usuarioService.cadastrarUsuario(any(UsuarioRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("maria@mail.com"));
    }

    @Test
    void deveRetornarBadRequestAoCadastrarUsuarioInvalido() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO("", "email-invalido");

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void deveListarUsuariosPaginados() throws Exception {
        UsuarioResponseDTO response = new UsuarioResponseDTO(1L, "Maria", "maria@mail.com");

        when(usuarioService.listarUsuarios(any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Maria"));
    }

    @Test
    void deveBuscarUsuarioPorId() throws Exception {
        when(usuarioService.buscarUsuarioPorId(1L)).thenReturn(new UsuarioResponseDTO(1L, "Maria", "maria@mail.com"));

        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deveRetornarNotFoundAoBuscarUsuarioInexistente() throws Exception {
        when(usuarioService.buscarUsuarioPorId(99L))
                .thenThrow(new ResourceNotFoundException("Usuario com id 99 nao encontrado."));

        mockMvc.perform(get("/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuario com id 99 nao encontrado."));
    }

    @Test
    void deveAtualizarUsuario() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO("Maria Souza", "maria@mail.com");
        UsuarioResponseDTO response = new UsuarioResponseDTO(1L, "Maria Souza", "maria@mail.com");

        when(usuarioService.atualizarUsuario(eq(1L), any(UsuarioRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Souza"));
    }

    @Test
    void deveExcluirUsuario() throws Exception {
        mockMvc.perform(delete("/usuarios/1"))
                .andExpect(status().isNoContent());
    }
}
