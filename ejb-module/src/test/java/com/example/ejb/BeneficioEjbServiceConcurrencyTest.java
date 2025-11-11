package com.example.ejb;

import com.example.ejb.entity.Beneficio;
import com.example.ejb.exceptions.SaldoInsuficienteException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeneficioEjbServiceConcurrencyTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    static void setupEMF() {
        emf = Persistence.createEntityManagerFactory("test-pu");
    }

    @AfterAll
    static void tearDownEMF() {
        if (emf != null) emf.close();
    }

    private static BeneficioEjbService newService(EntityManager em) {
        var svc = new BeneficioEjbService();
        try {
            var f = BeneficioEjbService.class.getDeclaredField("em");
            f.setAccessible(true);
            f.set(svc, em);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return svc;
    }

    private static Long persist(EntityManager em, String nome, BigDecimal valor, boolean ativo) {
        em.getTransaction().begin();
        Beneficio b = new Beneficio();
        b.setNome(nome);
        b.setDescricao(nome);
        b.setValor(valor);
        b.setAtivo(ativo);
        em.persist(b);
        em.getTransaction().commit();
        return b.getId();
    }

    @Test
    void transferirConcorrenteMesmaOrigemDestinosDiferentes() throws Exception {
        EntityManager em = emf.createEntityManager();
        Long origem = persist(em, "O", new BigDecimal("100.00"), true);
        Long d1 = persist(em, "D1", new BigDecimal("0.00"), true);
        Long d2 = persist(em, "D2", new BigDecimal("0.00"), true);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        Runnable r1 = () -> {
            EntityManager emTx = emf.createEntityManager();
            var svc = newService(emTx);
            try {
                emTx.getTransaction().begin();
                start.await();
                svc.transferir(origem, d1, new BigDecimal("60.00"));
                emTx.getTransaction().commit();
            } catch (Exception e) {
                if (emTx.getTransaction().isActive()) emTx.getTransaction().rollback();
                throw new RuntimeException(e);
            } finally {
                emTx.close();
            }
        };
        Runnable r2 = () -> {
            EntityManager emTx = emf.createEntityManager();
            var svc = newService(emTx);
            try {
                emTx.getTransaction().begin();
                start.await();
                svc.transferir(origem, d2, new BigDecimal("60.00"));
                emTx.getTransaction().commit();
            } catch (RuntimeException e) {
                if (emTx.getTransaction().isActive()) emTx.getTransaction().rollback();
            } catch (Exception e) {
                if (emTx.getTransaction().isActive()) emTx.getTransaction().rollback();
                throw new RuntimeException(e);
            } finally {
                emTx.close();
            }
        };

        futures.add(pool.submit(r1));
        futures.add(pool.submit(r2));
        start.countDown();

        for (Future<?> f : futures) {
            try {
                f.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException ee) {
                if (!(ee.getCause() instanceof RuntimeException) ||
                    !(ee.getCause().getCause() instanceof SaldoInsuficienteException)) {
                    throw ee;
                }
            }
        }
        pool.shutdown();
        Beneficio bO = em.find(Beneficio.class, origem);
        Beneficio bD1 = em.find(Beneficio.class, d1);
        Beneficio bD2 = em.find(Beneficio.class, d2);

        assertTrue(bO.getValor().compareTo(BigDecimal.ZERO) >= 0);
                boolean algumaAplicada =
                bD1.getValor().compareTo(BigDecimal.ZERO) > 0 ||
                bD2.getValor().compareTo(BigDecimal.ZERO) > 0;
        assertTrue(algumaAplicada || bO.getValor().compareTo(new BigDecimal("100.00")) == 0);

        BigDecimal total = bO.getValor().add(bD1.getValor()).add(bD2.getValor());
        assertEquals(new BigDecimal("100.00"), total);

        em.close();
    }

    @Test
    void transferirConcorrenteCruzadaSemDeadlock() throws Exception {
        EntityManager em = emf.createEntityManager();
        Long a = persist(em, "A", new BigDecimal("100.00"), true);
        Long b = persist(em, "B", new BigDecimal("100.00"), true);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Void> c1 = () -> {
            EntityManager emTx = emf.createEntityManager();
            var svc = newService(emTx);
            try {
                emTx.getTransaction().begin();
                start.await();
                svc.transferir(a, b, new BigDecimal("30.00"));
                emTx.getTransaction().commit();
                return null;
            } finally {
                if (emTx.getTransaction().isActive()) emTx.getTransaction().rollback();
                emTx.close();
            }
        };
        Callable<Void> c2 = () -> {
            EntityManager emTx = emf.createEntityManager();
            var svc = newService(emTx);
            try {
                emTx.getTransaction().begin();
                start.await();
                svc.transferir(b, a, new BigDecimal("25.00"));
                emTx.getTransaction().commit();
                return null;
            } finally {
                if (emTx.getTransaction().isActive()) emTx.getTransaction().rollback();
                emTx.close();
            }
        };

        Future<Void> f1 = pool.submit(c1);
        Future<Void> f2 = pool.submit(c2);
        start.countDown();

        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        pool.shutdown();

        Beneficio aAfter = em.find(Beneficio.class, a);
        Beneficio bAfter = em.find(Beneficio.class, b);

        BigDecimal total = aAfter.getValor().add(bAfter.getValor());
        assertEquals(new BigDecimal("200.00"), total);
        assertTrue(aAfter.getValor().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(bAfter.getValor().compareTo(BigDecimal.ZERO) >= 0);

//        boolean houveMovimento = aAfter.getValor().compareTo(new BigDecimal("100.00")) != 0
//                || bAfter.getValor().compareTo(new BigDecimal("100.00")) != 0;

        em.close();
    }

    @Test
    void debitarConcorrenteNaoUltrapassaSaldo() throws Exception {
        EntityManager em = emf.createEntityManager();
        Long conta = persist(em, "C", new BigDecimal("50.00"), true);

        ExecutorService pool = Executors.newFixedThreadPool(3);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        Runnable debitTask = () -> {
            EntityManager emTx = emf.createEntityManager();
            var svc = newService(emTx);
            try {
                emTx.getTransaction().begin();
                start.await();
                svc.debitar(conta, new BigDecimal("30.00"));
                emTx.getTransaction().commit();
            } catch (RuntimeException e) {
                if (emTx.getTransaction().isActive()) emTx.getTransaction().rollback();
            } catch (Exception e) {
                if (emTx.getTransaction().isActive()) emTx.getTransaction().rollback();
                throw new RuntimeException(e);
            } finally {
                emTx.close();
            }
        };

        futures.add(pool.submit(debitTask));
        futures.add(pool.submit(debitTask));
        futures.add(pool.submit(debitTask));
        start.countDown();

        for (Future<?> f : futures) {
            try {
                f.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException ee) {
                if (!(ee.getCause() instanceof RuntimeException) ||
                        !(ee.getCause().getCause() instanceof SaldoInsuficienteException)) {
                    throw ee;
                }
            }
        }
        pool.shutdown();

        Beneficio cAfter = em.find(Beneficio.class, conta);
        assertTrue(cAfter.getValor().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(cAfter.getValor().compareTo(new BigDecimal("50.00")) <= 0);
        em.close();
    }
}
