package com.example.ejb;

import com.example.ejb.entity.Beneficio;
import com.example.ejb.exceptions.OperacaoInvalidaException;
import com.example.ejb.exceptions.SaldoInsuficienteException;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;


import java.math.BigDecimal;
import java.util.List;

@Stateless
public final class BeneficioEjbService {

    @PersistenceContext
    private EntityManager em;

    public Beneficio findById(Long id) {
        Beneficio b = em.find(Beneficio.class, id);
        if (b == null) {
            throw new OperacaoInvalidaException("Benefício não encontrado: " + id);
        }
        return b;
    }

    public List<Beneficio> listAll() {
        return em.createQuery("select b from Beneficio b", Beneficio.class).getResultList();
    }

    public Beneficio create(Beneficio novo) {
        if (novo == null) throw new OperacaoInvalidaException("Payload inválido");
        if (novo.getNome() == null || novo.getNome().isBlank()) {
            throw new OperacaoInvalidaException("Nome é obrigatório");
        }
        if (novo.getValor() == null) {
            novo.setValor(new BigDecimal("0.00"));
        }
        em.persist(novo);
        em.flush();
        return novo;
    }

    public Beneficio update(Long id, Beneficio changes) {
        if (id == null || changes == null) throw new OperacaoInvalidaException("Parâmetros inválidos");
        Beneficio atual = em.find(Beneficio.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (atual == null) {
            throw new OperacaoInvalidaException("Benefício não encontrado: " + id);
        }
        if (changes.getNome() != null) atual.setNome(changes.getNome());
        if (changes.getDescricao() != null) atual.setDescricao(changes.getDescricao());
        if (changes.getValor() != null) atual.setValor(changes.getValor());
        if (changes.getAtivo() != null) atual.setAtivo(changes.getAtivo());
        em.flush();
        return atual;
    }

    public void delete(Long id) {
        Beneficio b = em.find(Beneficio.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (b == null) {
            throw new OperacaoInvalidaException("Benefício não encontrado: " + id);
        }
        em.remove(b);
        em.flush();
    }

    public void creditar(Long beneficioId, BigDecimal valor) {
        validarValorPositivo(valor);
        Beneficio b = em.find(Beneficio.class, beneficioId, LockModeType.PESSIMISTIC_WRITE);
        if (b == null || Boolean.FALSE.equals(b.getAtivo())) {
            throw new OperacaoInvalidaException("Benefício inválido/inativo: " + beneficioId);
        }
        b.creditar(valor);
        em.flush();
    }

    public void debitar(Long beneficioId, BigDecimal valor) {
        validarValorPositivo(valor);
        Beneficio b = em.find(Beneficio.class, beneficioId, LockModeType.PESSIMISTIC_WRITE);
        if (b == null || Boolean.FALSE.equals(b.getAtivo())) {
            throw new OperacaoInvalidaException("Benefício inválido/inativo: " + beneficioId);
        }
        if (b.getValor().compareTo(valor) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente no benefício " + beneficioId);
        }
        b.debitar(valor);
        em.flush();
    }

    public void transferir(Long origemId, Long destinoId, BigDecimal valor) {
        validarValorPositivo(valor);
        if (origemId.equals(destinoId)) {
            throw new OperacaoInvalidaException("Conta de origem e destino devem ser diferentes");
        }

        if (origemId < destinoId) {
            em.find(Beneficio.class, origemId, LockModeType.PESSIMISTIC_WRITE);
            em.find(Beneficio.class, destinoId, LockModeType.PESSIMISTIC_WRITE);
        } else {
            em.find(Beneficio.class, destinoId, LockModeType.PESSIMISTIC_WRITE);
            em.find(Beneficio.class, origemId, LockModeType.PESSIMISTIC_WRITE);
        }

        // Lock pessimista mantendo os mesmos nomes das variáveis
        Beneficio origem = em.find(Beneficio.class, origemId, LockModeType.PESSIMISTIC_WRITE);
        Beneficio destino = em.find(Beneficio.class, destinoId, LockModeType.PESSIMISTIC_WRITE);

        if (origem == null || destino == null) {
            throw new OperacaoInvalidaException("Benefício de origem/destino não encontrado");
        }
        if (Boolean.FALSE.equals(origem.getAtivo()) || Boolean.FALSE.equals(destino.getAtivo())) {
            throw new OperacaoInvalidaException("Benefício inativo (origem/destino)");
        }

        if (origem.getValor().compareTo(valor) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente no benefício de origem");
        }

        try {
            origem.debitar(valor);
            destino.creditar(valor);
            em.flush();
        } catch (OptimisticLockException e) {
            throw new OperacaoInvalidaException("Conflito de concorrência. Tente novamente.");
        }
    }

    private void validarValorPositivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new OperacaoInvalidaException("Valor deve ser positivo");
        }
    }
}
