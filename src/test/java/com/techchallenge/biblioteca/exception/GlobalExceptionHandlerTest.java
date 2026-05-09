package com.techchallenge.biblioteca.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveRetornarConflictParaViolacaoDeIntegridade() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/livros");

        var response = handler.handleDataIntegrityViolation(new DataIntegrityViolationException("constraint"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/livros");
    }

    @Test
    void deveRetornarConflictParaConflitoDeConcorrencia() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/livros/1");

        var response = handler.handleOptimisticLockingFailure(
                new OptimisticLockingFailureException("stale version"),
                request
        );

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("The resource was modified concurrently. Please reload and try again.");
    }

    @Test
    void deveRetornarErroInternoParaExcecaoGenerica() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/erro");

        var response = handler.handleException(new RuntimeException("falha"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected internal error occurred.");
    }
}
