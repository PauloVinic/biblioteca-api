package com.techchallenge.biblioteca.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.techchallenge.biblioteca.config.OpenApiConfig;
import com.techchallenge.biblioteca.dto.RelatorioLivroEmprestadoDTO;
import com.techchallenge.biblioteca.dto.RelatorioLivroMaisEmprestadoDTO;
import com.techchallenge.biblioteca.exception.GlobalExceptionHandler;
import com.techchallenge.biblioteca.service.RelatorioService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RelatorioController.class)
@Import({GlobalExceptionHandler.class, OpenApiConfig.class})
class RelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RelatorioService relatorioService;

    @Test
    void deveListarLivrosMaisEmprestados() throws Exception {
        when(relatorioService.listarLivrosMaisEmprestados())
                .thenReturn(List.of(new RelatorioLivroMaisEmprestadoDTO(1L, "Clean Code", "Robert C. Martin", "ISBN-001", 8L)));

        mockMvc.perform(get("/relatorios/livros-mais-emprestados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantidadeEmprestimos").value(8));
    }

    @Test
    void deveListarLivrosEmprestadosNoMomento() throws Exception {
        when(relatorioService.listarLivrosEmprestadosNoMomento())
                .thenReturn(List.of(new RelatorioLivroEmprestadoDTO(
                        10L,
                        1L,
                        "Clean Code",
                        2L,
                        "Maria",
                        LocalDate.now(),
                        LocalDate.now().plusDays(7)
                )));

        mockMvc.perform(get("/relatorios/livros-emprestados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Clean Code"));
    }
}
