package com.example.ejb;

import com.example.ejb.entity.Beneficio;
import com.example.ejb.exceptions.OperacaoInvalidaException;
import com.example.ejb.exceptions.SaldoInsuficienteException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeneficioEjbServiceTest {

    private static final String DESTINO_OK = "Destino OK";
    private static final String AMOUNT_TEN_DOLLARS = "10.00";
    private static final String ORIGEM_OK = "Origem OK";
    private static final String INATIVO = "Inativo";
    private static EntityManagerFactory emf;
    private EntityManager em;
    private BeneficioEjbService service;

    @BeforeAll
    static void setupEMF() {
        emf = Persistence.createEntityManagerFactory("test-pu");
    }

    @AfterAll
    static void tearDownEMF() {
        if (emf != null) emf.close();
    }

    @BeforeEach
    void setup() {
        em = emf.createEntityManager();
        service = new com.example.ejb.BeneficioEjbService();
        try {
            var f = com.example.ejb.BeneficioEjbService.class.getDeclaredField("em");
            f.setAccessible(true);
            f.set(service, em);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        em.getTransaction().begin();
        em.createQuery("delete from Beneficio").executeUpdate();

        em.persist(novo(ORIGEM_OK, "o1", new BigDecimal("100.00"), true));
        em.persist(novo(DESTINO_OK, "d1", new BigDecimal("50.00"), true));
        em.persist(novo(INATIVO, "i1", new BigDecimal("200.00"), false));
        em.getTransaction().commit();
    }

    private Beneficio novo(String nome, String desc, BigDecimal valor, boolean ativo) {
        var b = new Beneficio();
        b.setNome(nome);
        b.setDescricao(desc);
        b.setValor(valor);
        b.setAtivo(ativo);
        return b;
    }

    private Long idPorNome(String nome) {
        return em.createQuery("select b.id from Beneficio b where b.nome = :n", Long.class)
                .setParameter("n", nome)
                .getSingleResult();
    }

    @Test
    void findByIdOk() {
        Long id = idPorNome(ORIGEM_OK);
        var b = service.findById(id);
        assertEquals(ORIGEM_OK, b.getNome());
    }

    @Test
    void findByIdInexistente() {
        assertThrows(OperacaoInvalidaException.class, () -> service.findById(999L));
    }

    @Test
    void listAllOk() {
        List<Beneficio> lista = service.listAll();
        assertEquals(3, lista.size());
    }

    @Test
    void creditarOk() {
        Long id = idPorNome(DESTINO_OK);
        em.getTransaction().begin();
        service.creditar(id, new BigDecimal("25.00"));
        em.getTransaction().commit();

        var b = em.find(Beneficio.class, id);
        assertEquals(new BigDecimal("75.00"), b.getValor());
    }

    @Test
    void creditarValorInvalido() {
        Long id = idPorNome(DESTINO_OK);
        assertThrows(OperacaoInvalidaException.class, () -> {
            em.getTransaction().begin();
            try {
                service.creditar(id, BigDecimal.ZERO);
            } finally {
                em.getTransaction().rollback();
            }
        });
    }

    @Test
    void creditarInativo() {
        Long id = idPorNome(INATIVO);
        assertThrows(OperacaoInvalidaException.class, () -> {
            em.getTransaction().begin();
            try {
                service.creditar(id, new BigDecimal("10"));
            } finally {
                em.getTransaction().rollback();
            }
        });
    }

    @Test
    void debitarOk() {
        Long id = idPorNome(ORIGEM_OK);
        em.getTransaction().begin();
        service.debitar(id, new BigDecimal("40.00"));
        em.getTransaction().commit();

        var b = em.find(Beneficio.class, id);
        assertEquals(new BigDecimal("60.00"), b.getValor());
    }

    @Test
    void debitarSaldoInsuficiente() {
        Long id = idPorNome(DESTINO_OK);
        assertThrows(SaldoInsuficienteException.class, () -> {
            em.getTransaction().begin();
            try {
                service.debitar(id, new BigDecimal("1000.00"));
            } finally {
                em.getTransaction().rollback();
            }
        });
    }

    @Test
    void debitarInativo() {
        Long id = idPorNome(INATIVO);
        assertThrows(OperacaoInvalidaException.class, () -> {
            em.getTransaction().begin();
            try {
                service.debitar(id, new BigDecimal(AMOUNT_TEN_DOLLARS));
            } finally {
                em.getTransaction().rollback();
            }
        });
    }

    @Test
    void transferirOk() {
        Long origem = idPorNome(ORIGEM_OK);
        Long destino = idPorNome(DESTINO_OK);

        em.getTransaction().begin();
        service.transferir(origem, destino, new BigDecimal("30.00"));
        em.getTransaction().commit();

        assertEquals(new BigDecimal("70.00"), em.find(Beneficio.class, origem).getValor());
        assertEquals(new BigDecimal("80.00"), em.find(Beneficio.class, destino).getValor());
    }

    @Test
    void transferirMesmaConta() {
        Long origem = idPorNome(ORIGEM_OK);
        assertThrows(OperacaoInvalidaException.class, () -> {
            em.getTransaction().begin();
            try {
                service.transferir(origem, origem, new BigDecimal(AMOUNT_TEN_DOLLARS));
            } finally {
                em.getTransaction().rollback();
            }
        });
    }

    @Test
    void transferirSaldoInsuficiente() {
        Long origem = idPorNome(DESTINO_OK); // tem 50
        Long destino = idPorNome(ORIGEM_OK);
        assertThrows(SaldoInsuficienteException.class, () -> {
            em.getTransaction().begin();
            try {
                service.transferir(origem, destino, new BigDecimal("100.00"));
            } finally {
                em.getTransaction().rollback();
            }
        });
    }

    @Test
    void transferirInativo() {
        Long origem = idPorNome(INATIVO);
        Long destino = idPorNome(DESTINO_OK);
        assertThrows(OperacaoInvalidaException.class, () -> {
            em.getTransaction().begin();
            try {
                service.transferir(origem, destino, new BigDecimal(AMOUNT_TEN_DOLLARS));
            } finally {
                em.getTransaction().rollback();
            }
        });
    }
}