package com.example.backend.controller;

import com.example.ejb.BeneficioEjbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1/beneficios")
@Tag(name = "Benefícios", description = "Operações com benefícios")
public class BeneficioController {


    private final BeneficioEjbService ejbService;

    @Autowired
    public BeneficioController(BeneficioEjbService ejbService) {
        this.ejbService = ejbService;
    }


    @Operation(summary = "Buscar benefício por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@Parameter(description = "ID do benefício") @PathVariable Long id) {
        var beneficio = ejbService.findById(id);
        return ResponseEntity.ok(beneficio);
    }

    @Operation(summary = "Creditar valor em um benefício")
    @PostMapping("/{id}/creditos")
    public ResponseEntity<?> creditar(
            @Parameter(description = "ID do benefício") @PathVariable Long id,
            @Parameter(description = "Valor a creditar") @RequestParam BigDecimal valor) {
        ejbService.creditar(id, valor);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Debitar valor de um benefício")
    @PostMapping("/{id}/debitos")
    public ResponseEntity<?> debitar(
            @Parameter(description = "ID do benefício") @PathVariable Long id,
            @Parameter(description = "Valor a debitar") @RequestParam BigDecimal valor) {
        ejbService.debitar(id, valor);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Transferir entre benefícios")
    @PostMapping("/transferencias")
    public ResponseEntity<?> transferir(
            @Parameter(description = "ID de origem") @RequestParam Long origemId,
            @Parameter(description = "ID de destino") @RequestParam Long destinoId,
            @Parameter(description = "Valor a transferir") @RequestParam BigDecimal valor) {
        ejbService.transferir(origemId, destinoId, valor);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Listar todos os benefícios")
    @GetMapping
    public List<com.example.ejb.entity.Beneficio> list() {
        return ejbService.listAll();
    }

    @Operation(summary = "Criar benefício")
    @PostMapping
    public ResponseEntity<com.example.ejb.entity.Beneficio> criarBeneficio(@RequestBody com.example.ejb.entity.Beneficio body) {
        var created = ejbService.create(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Atualizar benefício")
    @PutMapping("/{id}")
    public ResponseEntity<com.example.ejb.entity.Beneficio> atualizarBeneficio(@PathVariable Long id, @RequestBody com.example.ejb.entity.Beneficio body) {
        var updated = ejbService.update(id, body);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Excluir benefício")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirBeneficio(@PathVariable Long id) {
        ejbService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
