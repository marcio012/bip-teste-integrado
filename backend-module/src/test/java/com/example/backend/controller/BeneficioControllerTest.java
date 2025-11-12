package com.example.backend.controller;

import com.example.ejb.BeneficioEjbService;
import com.example.ejb.entity.Beneficio;
import com.example.ejb.exceptions.OperacaoInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BeneficioControllerTest {

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
    void getBeneficioOk() throws Exception {
        when(ejb.findById(1L)).thenReturn(novo(1L, "A", "d", "10.00", true));
        mvc.perform(get("/api/v1/beneficios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("A")));
    }

    @Test
    void listBeneficiosOk() throws Exception {
        when(ejb.listAll()).thenReturn(List.of(
                novo(1L, "A","d","10.00", true),
                novo(2L, "B","e","20.00", true)
        ));
        mvc.perform(get("/api/v1/beneficios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void createBeneficioCreated() throws Exception {
        when(ejb.create(any(Beneficio.class))).thenReturn(novo(10L, "Novo", "Desc", "0.00", true));
        String body = "{\"nome\":\"Novo\",\"descricao\":\"Desc\",\"valor\":0.00,\"ativo\":true}";

        mvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.nome", is("Novo")));
    }

    @Test
    void createBeneficioBadRequestNomeVazio() throws Exception {
        String body = "{\"nome\":\" \",\"descricao\":\"x\",\"valor\":0.00,\"ativo\":true}";
        when(ejb.create(any(Beneficio.class))).thenThrow(new OperacaoInvalidaException("Nome é obrigatório"));

        mvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBeneficioBadRequestNomeVazioControllerValida() throws Exception {
        String body = "{\"nome\":\" \",\"descricao\":\"x\",\"valor\":0.00,\"ativo\":true}";
        mvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
        verify(ejb, never()).create(any());
    }

    @Test
    void updateBeneficioOk() throws Exception {
        when(ejb.update(eq(1L), any(Beneficio.class)))
                .thenReturn(novo(1L, "Upd", "Nova", "15.00", false));
        String body = "{\"nome\":\"Upd\",\"descricao\":\"Nova\",\"valor\":15.00,\"ativo\":false}";

        mvc.perform(put("/api/v1/beneficios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor", is(15.00)))
                .andExpect(jsonPath("$.ativo", is(false)));
    }

    @Test
    void deleteBeneficioNoContent() throws Exception {
        doNothing().when(ejb).delete(1L);

        mvc.perform(delete("/api/v1/beneficios/1"))
                .andExpect(status().isNoContent());
    }

    private Beneficio novo(Long id, String nome, String desc, String valor, boolean ativo) {
        var b = new Beneficio();
        b.setId(id);
        b.setNome(nome);
        b.setDescricao(desc);
        b.setValor(new BigDecimal(valor));
        b.setAtivo(ativo);
        return b;
    }

}