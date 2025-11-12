package com.example.backend.controller;

import com.example.ejb.BeneficioEjbService;
import com.example.ejb.exceptions.OperacaoInvalidaException;
import com.example.ejb.exceptions.SaldoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BeneficioControllerOperacoesTest {

    private MockMvc mvc;
    private BeneficioEjbService ejb;

    @BeforeEach
    void setup() {
        ejb = mock(BeneficioEjbService.class);
        var controller = new BeneficioController(ejb);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void creditarNoContent() throws Exception {
        doNothing().when(ejb).creditar(1L, new BigDecimal("50.00"));
        mvc.perform(post("/api/v1/beneficios/1/creditos")
                        .param("valor", "50.00"))
                .andExpect(status().isNoContent());
    }

    @Test
    void debitarNoContent() throws Exception {
        doNothing().when(ejb).debitar(1L, new BigDecimal("25.00"));
        mvc.perform(post("/api/v1/beneficios/1/debitos")
                        .param("valor", "25.00"))
                .andExpect(status().isNoContent());
    }

    @Test
    void debitarSaldoInsuficiente422() throws Exception {
        doThrow(new SaldoInsuficienteException("saldo")).when(ejb).debitar(1L, new BigDecimal("999.00"));
        mvc.perform(post("/api/v1/beneficios/1/debitos")
                        .param("valor", "999.00"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void transferirNoContent() throws Exception {
        doNothing().when(ejb).transferir(1L, 2L, new BigDecimal("10.00"));
        mvc.perform(post("/api/v1/beneficios/transferencias")
                        .param("origemId", "1")
                        .param("destinoId", "2")
                        .param("valor", "10.00"))
                .andExpect(status().isNoContent());
    }

    @Test
    void transferirOperacaoInvalida400() throws Exception {
        doThrow(new OperacaoInvalidaException("mesma conta"))
                .when(ejb).transferir(1L, 1L, new BigDecimal("10.00"));

        mvc.perform(post("/api/v1/beneficios/transferencias")
                        .param("origemId", "1")
                        .param("destinoId", "1")
                        .param("valor", "10.00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBeneficioNotFound_MapeadoComo400() throws Exception {
        doThrow(new OperacaoInvalidaException("nao encontrado")).when(ejb).findById(999L);

        mvc.perform(get("/api/v1/beneficios/999"))
                .andExpect(status().isBadRequest());
    }
}