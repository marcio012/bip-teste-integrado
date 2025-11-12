package com.example.backend.controller;

import com.example.ejb.BeneficioEjbService;
import com.example.ejb.exceptions.OperacaoInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestExceptionHandlerTest {

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
    void genericException_mapeadaPara500() throws Exception {
        when(ejb.findById(1L)).thenThrow(new RuntimeException("boom"));
        mvc.perform(get("/api/v1/beneficios/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void operacaoInvalida_mapeadaPara400() throws Exception {
        when(ejb.findById(999L)).thenThrow(new OperacaoInvalidaException("invalida"));
        mvc.perform(get("/api/v1/beneficios/999"))
                .andExpect(status().isBadRequest());
    }
}
