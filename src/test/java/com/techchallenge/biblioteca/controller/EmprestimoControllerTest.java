package com.techchallenge.biblioteca.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.biblioteca.config.OpenApiConfig;
import com.techchallenge.biblioteca.dto.DevolucaoRequestDTO;
import com.techchallenge.biblioteca.dto.EmprestimoRequestDTO;
import com.techchallenge.biblioteca.dto.EmprestimoResponseDTO;
import com.techchallenge.biblioteca.enums.StatusEmprestimo;
import com.techchallenge.biblioteca.exception.BusinessException;
import com.techchallenge.biblioteca.exception.GlobalExceptionHandler;
import com.techchallenge.biblioteca.service.EmprestimoService;
import java.time.LocalDate;
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

@WebMvcTest(EmprestimoController.class)
@Import({GlobalExceptionHandler.class, OpenApiConfig.class})
class EmprestimoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmprestimoService emprestimoService;

    @Test
    void deveRealizarEmprestimo() throws Exception {
        EmprestimoRequestDTO request = new EmprestimoRequestDTO(1L, 2L, 7);
        EmprestimoResponseDTO response = criarResponse(StatusEmprestimo.EMPRESTADO);

        when(emprestimoService.realizarEmprestimo(any(EmprestimoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EMPRESTADO"));
    }

    @Test
    void deveRetornarBadRequestQuandoEmprestimoNegadoPorRegraDeNegocio() throws Exception {
        EmprestimoRequestDTO request = new EmprestimoRequestDTO(1L, 2L, 7);

        when(emprestimoService.realizarEmprestimo(any(EmprestimoRequestDTO.class)))
                .thenThrow(new BusinessException("O livro informado nao esta disponivel para emprestimo."));

        mockMvc.perform(post("/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O livro informado nao esta disponivel para emprestimo."));
    }

    @Test
    void deveRegistrarDevolucao() throws Exception {
        DevolucaoRequestDTO request = new DevolucaoRequestDTO(10L);
        EmprestimoResponseDTO response = criarResponse(StatusEmprestimo.DEVOLVIDO);

        when(emprestimoService.registrarDevolucao(any(DevolucaoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/emprestimos/devolucao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEVOLVIDO"));
    }

    @Test
    void deveListarEmprestimosPorStatus() throws Exception {
        EmprestimoResponseDTO response = criarResponse(StatusEmprestimo.EMPRESTADO);

        when(emprestimoService.listarEmprestimosPorStatus(eq(StatusEmprestimo.EMPRESTADO), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/emprestimos").param("status", "EMPRESTADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("EMPRESTADO"));
    }

    @Test
    void deveRetornarBadRequestQuandoStatusForInvalido() throws Exception {
        mockMvc.perform(get("/emprestimos").param("status", "INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for request parameter 'status'."));
    }

    @Test
    void deveRetornarBadRequestQuandoCorpoForMalformado() throws Exception {
        mockMvc.perform(post("/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"livroId\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is invalid or malformed."));
    }

    @Test
    void deveRetornarBadRequestQuandoQuantidadeDiasUltrapassarLimite() throws Exception {
        EmprestimoRequestDTO request = new EmprestimoRequestDTO(1L, 2L, 91);

        mockMvc.perform(post("/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    private EmprestimoResponseDTO criarResponse(StatusEmprestimo status) {
        return new EmprestimoResponseDTO(
                10L,
                1L,
                "Clean Code",
                2L,
                "Maria",
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                status == StatusEmprestimo.DEVOLVIDO ? LocalDate.now() : null,
                status
        );
    }
}
