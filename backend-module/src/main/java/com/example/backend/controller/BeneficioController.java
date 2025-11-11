package com.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/beneficios")
@Tag(name = "Benefícios", description = "Operações com benefícios")
public class BeneficioController {

    private com.example.ejb.BeneficioEjbService ejb() {
        try {
            InitialContext ctx = new InitialContext();
            // Ajuste o nome JNDI conforme seu servidor/container
            return (com.example.ejb.BeneficioEjbService) ctx.lookup("java:global/ejb-module/BeneficioEjbService");
        } catch (NamingException e) {
            throw new IllegalStateException("Falha ao localizar EJB BeneficioEjbService via JNDI", e);
        }
    }

    @Operation(summary = "Buscar benefício por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@Parameter(description = "ID do benefício") @PathVariable Long id) {
        var b = ejb().findById(id);
        return ResponseEntity.ok(b);
    }

    @Operation(summary = "Creditar valor em um benefício")
    @PostMapping("/{id}/creditos")
    public ResponseEntity<?> creditar(
            @Parameter(description = "ID do benefício") @PathVariable Long id,
            @Parameter(description = "Valor a creditar") @RequestParam BigDecimal valor) {
        ejb().creditar(id, valor);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Debitar valor de um benefício")
    @PostMapping("/{id}/debitos")
    public ResponseEntity<?> debitar(
            @Parameter(description = "ID do benefício") @PathVariable Long id,
            @Parameter(description = "Valor a debitar") @RequestParam BigDecimal valor) {
        ejb().debitar(id, valor);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Transferir entre benefícios")
    @PostMapping("/transferencias")
    public ResponseEntity<?> transferir(
            @Parameter(description = "ID de origem") @RequestParam Long origemId,
            @Parameter(description = "ID de destino") @RequestParam Long destinoId,
            @Parameter(description = "Valor a transferir") @RequestParam BigDecimal valor) {
        ejb().transferir(origemId, destinoId, valor);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public List<String> list() {
        return Arrays.asList("Beneficio A", "Beneficio B");
    }
}
