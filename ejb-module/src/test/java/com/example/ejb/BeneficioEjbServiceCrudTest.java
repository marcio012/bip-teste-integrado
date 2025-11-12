package com.example.ejb;

import com.example.ejb.entity.Beneficio;
import com.example.ejb.exceptions.OperacaoInvalidaException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for BeneficioEjbService, providing unit tests for CRUD (Create, Read,
 * Update, Delete) operations on the entity Beneficio using JUnit framework.
 *
 * This test class verifies the correct behavior of BeneficioEjbService methods
 * involved in entity management, ensuring the service's expected behaviors
 * in multiple scenarios, such as successful creation, updating, and deletion,
 * as well as exceptional conditions.
 *
 * The tests leverage an in-memory database setup using JPA EntityManager and
 * EntityManagerFactory for reproducible and isolated test conditions.
 *
 * Tests include:
 * - Creation of a new Beneficio entity (with required fields validation).
 * - Updating an existing Beneficio entity attributes with new values.
 * - Deleting an existing Beneficio entity.
 * - Handling exceptional scenarios such as updating or deleting a non-existent entity.
 *
 * Key operations:
 * - Setup of EntityManager and EntityManagerFactory before tests execution.
 * - Cleanup of the database state between tests to maintain isolation.
 * - Use of assertions to validate outcomes: correctness of data persistence,
 *   updates, and exception handling.
 *
 * Dependencies:
 * - JPA EntityManager for data persistence.
 * - JUnit for testing framework and lifecycle management.
 */
public class BeneficioEjbServiceCrudTest {

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
    void setup() throws Exception {
        em = emf.createEntityManager();
        service = new BeneficioEjbService();
        var f = BeneficioEjbService.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(service, em);

        em.getTransaction().begin();
        em.createQuery("delete from Beneficio").executeUpdate();
        em.getTransaction().commit();
    }

    /**
     * Testa o processo de criação de um novo registro de benefício.
     *
     * Este método valida a funcionalidade de criar uma entidade do tipo Beneficio,
     * persistindo-a no banco de dados e verificando se os atributos definidos no
     * objeto criado correspondem aos valores persistidos.
     *
     * Fluxo do teste:
     * - Cria um objeto Beneficio com valores iniciais para os atributos nome,
     *   descrição, valor e ativo.
     * - Inicia uma transação no contexto de persistência.
     * - Chama o método de criação do serviço para persistir o objeto Beneficio.
     * - Confirma a transação de persistência.
     * - Valida se o ID do objeto criado não é nulo, indicando que foi gerado e
     *   persistido corretamente.
     * - Recupera a entidade persistida do banco*/
    @Test
    void criaBeneficio() {
        var novo = new Beneficio();
        novo.setNome("Novo");
        novo.setDescricao("Desc");
        novo.setValor(new BigDecimal("10.00"));
        novo.setAtivo(true);

        em.getTransaction().begin();
        Beneficio created = service.create(novo);
        em.getTransaction().commit();

        assertNotNull(created.getId());
        Beneficio db = em.find(Beneficio.class, created.getId());
        assertEquals("Novo", db.getNome());
        assertEquals(new BigDecimal("10.00"), db.getValor());
    }

    /**
     * Testa a validação de obrigatoriedade do atributo "nome" ao criar um registro de benefício.
     *
     * Este método assegura que uma exceção do tipo OperacaoInvalidaException seja lançada
     * ao tentar persistir um objeto do tipo Beneficio sem um valor definido para o atributo "nome".
     *
     * Fluxo do teste:
     * - Cria um objeto Beneficio com valor inicial definido como 0.00, mas sem um nome.
     * - Inicia uma transação no contexto de persistência.
     * - Chama o método de criação do serviço para tentar persistir o objeto.
     * - Verifica se a exceção apropriada é lançada.
     * - Faz o rollback da transação para evitar operações inválidas persistidas no banco.
     *
     * Objetivo:
     * Validar a regra de negócio que define "nome" como um atributo obrigatório para a criação
     * de registros de benefício na aplicação.
     */
    @Test
    void criaBeneficioNomeObrigatorio() {
        var novo = new Beneficio();
        novo.setValor(new BigDecimal("0.00"));
        em.getTransaction().begin();
        assertThrows(OperacaoInvalidaException.class, () -> service.create(novo));
        em.getTransaction().rollback();
    }

    /**
     * Testa o processo de atualização de um registro de benefício existente.
     *
     * Este método valida a funcionalidade de atualização de uma entidade do tipo Beneficio,
     * verificando se os valores dos atributos modificados no objeto original
     * são corretamente persistidos no banco de dados.
     *
     * Fluxo do teste:
     * - Insere um registro de benefício no banco de dados com valores iniciais para os campos
     *   nome, descrição, valor e ativo, utilizando o método auxiliar 'seed'.
     * - Cria um objeto Beneficio contendo as alterações desejadas nos mesmos campos.
     * - Inicia uma transação no contexto de persistência.
     * - Chama o método de atualização do serviço para aplicar as alterações no registro original.
     * - Confirma a transação de persistência.
     * - Valida se os atributos do registro atualizado no banco de dados correspondem
     *   aos valores definidos no objeto de alterações.
     *
     * Validações realizadas:
     * - Verifica se o nome do benefício foi atualizado corretamente.
     * - Verifica se a descrição do benefício foi atualizada corretamente.
     * - Verifica se o valor do benefício foi atualizado corretamente.
     * - Verifica se o atributo que indica se o benefício está ativo foi atualizado corretamente.
     */
    @Test
    void atualizaBeneficio() {
        Long id = seed("Antigo", "x", new BigDecimal("5.00"), true);

        var changes = new Beneficio();
        changes.setNome("Atualizado");
        changes.setDescricao("Nova");
        changes.setValor(new BigDecimal("15.00"));
        changes.setAtivo(false);

        em.getTransaction().begin();
        Beneficio upd = service.update(id, changes);
        em.getTransaction().commit();

        assertEquals("Atualizado", upd.getNome());
        assertEquals("Nova", upd.getDescricao());
        assertEquals(new BigDecimal("15.00"), upd.getValor());
        assertFalse(upd.getAtivo());
    }

    /**
     * Testa a tentativa de atualização de um registro de benefício inexistente.
     *
     * Este método valida a regra de negócio que impede a modificação de registros
     * que não estão presentes na base de dados. Ao tentar atualizar um registro
     * usando um ID que não corresponde a nenhum benefício existente, o sistema
     * deve lançar uma exceção indicando a invalidez da operação.
     *
     * Fluxo do teste:
     * - Cria um objeto Beneficio representando as alterações que seriam realizadas.
     * - Define o atributo "nome" do objeto de alterações.
     * - Inicia uma transação no contexto de persistência.
     * - Tenta realizar a atualização utilizando um ID inexistente (999L) e verifica
     *   se a exceção do tipo OperacaoInvalidaException é lançada corretamente.
     * - Realiza o rollback da transação para garantir que nenhuma alteração seja
     *   aplicada no banco de dados devido ao teste.
     *
     * Objetivo:
     * - Garantir a integridade dos dados ao impedir operações que envolvem registros
     *   inexistentes, mantendo as regras de negócio e a robustez da aplicação.
     */
    @Test
    void atualizaBeneficioInexistente() {
        var changes = new Beneficio();
        changes.setNome("x");
        em.getTransaction().begin();
        assertThrows(OperacaoInvalidaException.class, () -> service.update(999L, changes));
        em.getTransaction().rollback();
    }

    /**
     * Testa o processo de exclusão de um registro de benefício existente.
     *
     * Este método valida a funcionalidade de exclusão de uma entidade do tipo Beneficio,
     * garantindo que o registro correspondente seja removido do banco de dados.
     *
     * Fluxo do teste:
     * - Insere um registro de benefício utilizando o método auxiliar "seed", que gera
     *   uma entidade com valores de teste persistidos no banco de dados.
     * - Inicia uma transação no contexto de persistência.
     * - Chama o método "delete" do serviço, passando o ID do registro criado para realizar
     *   a exclusão do benefício.
     * - Confirma a transação de exclusão.
     * - Valida que o registro não está mais presente no banco de dados, verificando
     *   que o método "find" retorna null.
     *
     * Objetivo:
     * Garantir que a funcionalidade de exclusão remove adequadamente registros válidos
     * da base de dados sem gerar erros ou inconsistências.
     */
    @Test
    void deletaBeneficio() {
        Long id = seed("Apagar", "x", new BigDecimal("1.00"), true);
        em.getTransaction().begin();
        service.delete(id);
        em.getTransaction().commit();

        assertNull(em.find(Beneficio.class, id));
    }

    /**
     * Testa a tentativa de exclusão de um registro de benefício inexistente.
     *
     * Este método valida a regra de negócio que impede a exclusão de registros que
     * não estão presentes na base de dados. Ao tentar excluir um registro utilizando
     * um ID que não corresponde a nenhum benefício existente, o sistema deve lançar
     * uma exceção indicando que a operação não é válida.
     *
     * Fluxo do teste:
     * - Inicia uma transação no contexto de persistência.
     * - Tenta executar o método de exclusão passando o ID de um registro inexistente (999L).
     * - Verifica se uma exceção do tipo OperacaoInvalidaException é lançada corretamente.
     * - Realiza o rollback da transação para garantir que nenhuma alteração seja persistida
     *   no banco de dados.
     *
     * Objetivo:
     * Garantir que a funcionalidade de exclusão respeita as regras de negócio, ao evitar
     * a remoção de registros inexistentes e informar corretamente os casos em que não há
     * entidade correspondente ao ID fornecido.
     */
    @Test
    void deletaBeneficioInexistente() {
        em.getTransaction().begin();
        assertThrows(OperacaoInvalidaException.class, () -> service.delete(999L));
        em.getTransaction().rollback();
    }

    /**
     * Persiste um registro do tipo Beneficio no banco de dados.
     *
     * Este método cria uma nova entidade do tipo Beneficio, define seus atributos
     * com os valores fornecidos como parâmetro, inicia uma transação no contexto
     * de persistência, persiste o registro e confirma a transação.
     *
     * @param nome o nome do benefício que será persistido.
     * @param desc a descrição do benefício que será persistido.
     * @param valor o valor monetário associado ao benefício.
     * @param ativo indica se o benefício está ativo (true) ou inativo (false).
     * @return o ID do benefício persistido, gerado automaticamente pelo banco de dados.
     */
    private Long seed(String nome, String desc, BigDecimal valor, boolean ativo) {
        em.getTransaction().begin();
        var b = new Beneficio();
        b.setNome(nome);
        b.setDescricao(desc);
        b.setValor(valor);
        b.setAtivo(ativo);
        em.persist(b);
        em.getTransaction().commit();
        return b.getId();
    }
}
