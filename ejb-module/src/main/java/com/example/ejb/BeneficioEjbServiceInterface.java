package com.example.ejb;

import com.example.ejb.entity.Beneficio;
import java.math.BigDecimal;
import java.util.List;

public interface BeneficioEjbServiceInterface {
    Beneficio findById(Long id);
    List<Beneficio> listAll();
    Beneficio create(Beneficio beneficio);
    Beneficio update(Long id, Beneficio beneficio);
    void delete(Long id);
    void creditar(Long id, BigDecimal valor);
    void debitar(Long id, BigDecimal valor);
    void transferir(Long origemId, Long destinoId, BigDecimal valor);
}