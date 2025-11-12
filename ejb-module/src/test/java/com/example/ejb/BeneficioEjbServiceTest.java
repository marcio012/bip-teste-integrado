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

/**
 * Classe de teste para a classe BeneficioEjbService.
 *
 * Esta classe contém testes unitários para validar o comportamento da lógica de negócios
 * implementada na classe BeneficioEjbService. Os testes cobrem casos de sucesso e falha
 * para operações como encontrar benefícios, listar todos os benefícios, creditar, debitar
 * e transferir valores entre benefícios.
 *
 * O EntityManager é configurado e gerenciado localmente nesta classe para garantir que
 * os testes possam rodar de forma isolada, utilizando um banco de dados de teste.
 *
 * Métodos de teste incluem:
 * - Teste para busca de benefícios por ID em casos válidos e inválidos.
 * - Teste para listagem de todos os benefícios ativos.
 * - Testes que validam o comportamento de creditar valores, incluindo condições inválidas
 *   como valores nulos, zero ou contas inativas.
 * - Testes que validam o comportamento de debitar valores, incluindo a checagem de saldo
 *   insuficiente ou contas inativas.
 * - Testes que validam transferências de valores entre diferentes contas, garantindo que
 *   situações como transferência para a mesma conta, saldo insuficiente ou contas inativas
 *   sejam corretamente tratadas.
 *
 * Garantia de consistência:
 * Para cada operação que envolve modificações no banco de dados, transações são iniciadas,
 * e revertidas quando necessário para garantir que o estado do banco permaneça consistente
 * após a execução dos testes.
 *
 * Exceções validadas:
 * - OperacaoInvalidaException: Lançada para operações inválidas, como tentativas de interação
 *   com contas inativas ou transferência para a mesma conta.
 * - SaldoInsuficienteException: Lançada para tentativas de débito ou transferência sem saldo
 *   suficiente.
 */
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

    /**
     * Cria e retorna uma nova instância de Beneficio com os valores fornecidos.
     *
     * @param nome o nome do benefício.
     * @param desc a descrição do benefício.
     * @param valor o valor financeiro associado ao benefício.
     * @param ativo indica se o benefício está ativo ou inativo.
     * @return uma nova instância da classe Beneficio com os valores fornecidos.
     */
    private Beneficio novo(String nome, String desc, BigDecimal valor, boolean ativo) {
        var b = new Beneficio();
        b.setNome(nome);
        b.setDescricao(desc);
        b.setValor(valor);
        b.setAtivo(ativo);
        return b;
    }

    /**
     * Obtém o identificador de um benefício com base no nome fornecido.
     *
     * @param nome o nome do benefício para o qual o identificador será recuperado
     * @return o identificador (ID) correspondente ao benefício com o nome especificado
     */
    private Long idPorNome(String nome) {
        return em.createQuery("select b.id from Beneficio b where b.nome = :n", Long.class)
                .setParameter("n", nome)
                .getSingleResult();
    }

    /**
     * Testa o método findById da classe de serviço para verificar a recuperação de um benefício
     * existente com base no identificador (ID).
     *
     * Este teste realiza os seguintes passos:
     * 1. Obtém o ID de um benefício existente com o nome definido como ORIGEM_OK.
     * 2. Recupera o benefício correspondente através do método findById do serviço.
     * 3. Valida se o nome do benefício recuperado corresponde ao nome esperado (ORIGEM_OK).
     *
     * Objetivo:
     * Garantir que o serviço consegue corretamente localizar e retornar um benefício ativo no banco
     * de dados a partir de um identificador válido.
     *
     * Condições esperadas:
     * - O método service.findById retorna uma instância válida de Beneficio.
     * - O nome do benefício retornado corresponde ao valor da constante ORIGEM_OK.
     *
     * Caso de falha:
     * Caso o benefício não seja encontrado ou os valores esperados não sejam retornados,
     * o teste falha.
     */
    @Test
    void findByIdOk() {
        Long id = idPorNome(ORIGEM_OK);
        var b = service.findById(id);
        assertEquals(ORIGEM_OK, b.getNome());
    }

    /**
     * Testa o método service.findById para verificar o comportamento ao tentar
     * recuperar um benefício inexistente no banco de dados.
     *
     * Este teste realiza os seguintes passos:
     * 1. Define um identificador inválido (999L) que não corresponde a nenhum
     *    benefício presente no banco de dados.
     * 2. Chama o método service.findById com o identificador inválido.
     * 3. Aguarda a ocorrência de uma exceção OperacaoInvalidaException.
     *
     * Objetivo:
     * Garantir que o método service.findById lança a exceção esperada
     * (OperacaoInvalidaException) ao tentar recuperar um benefício inexistente.
     *
     * Condições esperadas:
     * - O método service.findById deve lançar OperacaoInvalidaException.
     *
     * Caso de falha:
     * - O teste falha se a exceção OperacaoInvalidaException não for lançada
     *   ou se uma exceção diferente for gerada.
     */
    @Test
    void findByIdInexistente() {
        assertThrows(OperacaoInvalidaException.class, () -> service.findById(999L));
    }

    /**
     * Testa o método listAll do serviço para verificar a recuperação de todos os
     * registros da entidade Beneficio.
     *
     * Este teste realiza os seguintes passos:
     * 1. Invoca o método service.listAll para obter a lista de Beneficio armazenados.
     * 2. Verifica se o tamanho da lista retornada é igual ao número esperado de registros.
     *
     * Objetivo:
     * Garantir que o serviço consegue corretamente retornar todas as instâncias de
     * Beneficio presentes no banco de dados.
     *
     * Condições esperadas:
     * - O método service.listAll devolve uma lista contendo todas as instâncias de Beneficio
     *   armazenadas.
     * - O tamanho da lista é igual ao número esperado (neste caso, 3).
     *
     * Caso de falha:
     * Caso a lista retornada seja nula, vazia ou tenha um número de elementos diferente do
     * esperado, o teste falha.
     */
    @Test
    void listAllOk() {
        List<Beneficio> lista = service.listAll();
        assertEquals(3, lista.size());
    }

    /**
     * Testa o método service.creditar para verificar o comportamento ao adicionar um valor positivo
     * ao saldo de um benefício ativo.
     *
     * Este teste executa os seguintes passos:
     * 1. Obtém o identificador do benefício com base no nome definido como DESTINO_OK.
     * 2. Inicia uma transação.
     * 3. Chama o método service.creditar com o identificador do benefício e o valor 25.00.
     * 4. Confirma a transação.
     * 5. Recupera o benefício atualizado a partir do banco de dados.
     * 6. Verifica se o valor do benefício foi incrementado corretamente, somando 25.00.
     *
     * Objetivo:
     * Garantir que o método creditar adiciona o valor especificado ao saldo atual do benefício
     * ativo sem erros e de forma consistente.
     *
     * Condições esperadas:
     * - O valor do benefício é atualizado corretamente após a operação.
     * - O saldo final do benefício corresponde à soma do saldo anterior com o valor creditado,
     *   neste caso, 75.00.
     *
     * Caso de falha:
     * - O teste falha se o valor do benefício não for atualizado adequadamente
     *   ou se o método creditar lançar uma exceção inesperada.
     */
    @Test
    void creditarOk() {
        Long id = idPorNome(DESTINO_OK);
        em.getTransaction().begin();
        service.creditar(id, new BigDecimal("25.00"));
        em.getTransaction().commit();

        var b = em.find(Beneficio.class, id);
        assertEquals(new BigDecimal("75.00"), b.getValor());
    }

    /**
     * Testa o método service.creditar para verificar o comportamento ao tentar adicionar
     * um valor inválido ao saldo de um benefício ativo.
     *
     * Este teste executa os seguintes passos:
     * 1. Obtém o identificador do benefício com base no nome definido como DESTINO_OK.
     * 2. Inicia uma transação.
     * 3. Chama o método service.creditar com o identificador do benefício e um valor inválido (BigDecimal.ZERO).
     * 4. Aguarda a ocorrência de uma exceção OperacaoInvalidaException.
     * 5. Efetua o rollback da transação para garantir que nenhuma modificação é persistida.
     *
     * Objetivo:
     * Garantir que o método creditar lança a exceção esperada (OperacaoInvalidaException)
     * ao receber um valor inválido, como zero ou valores negativos, e que nenhuma alteração
     * é aplicada ao benefício.
     *
     * Condições esperadas:
     * - O método service.creditar deve lançar uma OperacaoInvalidaException ao tentar
     *   creditar um valor inválido.
     * - Nenhuma operação deve ser confirmada no banco de dados após execução com valores inválidos.
     *
     * Caso de falha:
     * - O teste falha se a exceção OperacaoInvalidaException não for lançada ou se uma exceção
     *   diferente for gerada.
     * - O teste falha se alterações indevidas forem aplicadas ao benefício no banco de dados.
     */
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

    /**
     * Testa o método creditar em uma conta com status inativo.
     *
     * O objetivo deste teste é verificar se uma exceção do tipo
     * OperacaoInvalidaException é lançada ao tentar realizar uma
     * operação de crédito em uma conta que está inativa.
     *
     * Cenário:
     * - Busca o ID de uma conta com status inativo.
     * - Inicia uma transação para simular a operação.
     * - Tenta realizar a operação de crédito na conta utilizando o ID e um valor definido.
     * - Garantia: a transação será revertida.
     *
     * Resultado esperado:
     * - A operação deve lançar uma exceção OperacaoInvalidaException.
     */
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

    /**
     * Testa o método debitar verificando se a operação de débito no benefício é realizada corretamente.
     *
     * O teste busca um ID de benefício baseado em um nome previamente definido, inicia uma transação,
     * realiza a operação de débito com um valor específico e finaliza a transação. Em seguida, realiza
     * uma consulta ao banco de dados para garantir que o valor debitado foi subtraído corretamente do valor inicial.
     *
     * Validações:
     * - O valor após o débito deve ser o valor esperado após subtração do débito aplicado.
     */
    @Test
    void debitarOk() {
        Long id = idPorNome(ORIGEM_OK);
        em.getTransaction().begin();
        service.debitar(id, new BigDecimal("40.00"));
        em.getTransaction().commit();

        var b = em.find(Beneficio.class, id);
        assertEquals(new BigDecimal("60.00"), b.getValor());
    }

    /**
     * Testa o cenário em que uma tentativa de débito é realizada em uma conta com saldo insuficiente.
     * O teste verifica se a exceção SaldoInsuficienteException é lançada corretamente.
     *
     * Cenário:
     * - Obtém o ID da conta a partir de um nome fictício.
     * - Tenta debitar um valor maior do que o saldo disponível na conta.
     * - Inicia uma transação e, em caso de exceção, realiza o rollback da transação.
     *
     * Exceções:
     * - SaldoInsuficienteException: Deve ser lançada quando não há saldo suficiente na conta para realizar o débito.
     */
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

    /**
     * Testa o comportamento do método debitar quando chamado em uma entidade inativa.
     *
     * O teste verifica se ao tentar debitar um valor de uma entidade cujo estado
     * está definido como inativo, uma exceção do tipo OperacaoInvalidaException
     * é lançada. O método utiliza uma transação que é sempre revertida ao final do teste,
     * garantindo que nenhuma alteração persista no banco de dados.
     *
     * Requisitos testados:
     * - A operação de débito em uma entidade inativa deve ser considerada inválida.
     * - A exceção apropriada (OperacaoInvalidaException) deve ser lançada.
     */
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

    /**
     * Testa o método de transferência entre contas, verificando o comportamento ao tentar
     * realizar uma transferência para a mesma conta de origem.
     *
     * O teste simula uma operação de transferência onde a conta de origem e destino
     * são a mesma, o que é uma operação inválida. Verifica se a exceção esperada
     * {@link OperacaoInvalidaException} é lançada corretamente nesses casos.
     *
     * O teste executa a operação dentro de uma transação, garantindo que nenhuma
     * alteração seja persistida no banco de dados, utilizando rollback no final.
     */
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

    /**
     * Testa o cenário de transferência de saldo em que o saldo da conta de origem é insuficiente.
     * O teste verifica se a exceção SaldoInsuficienteException é lançada ao tentar realizar
     * uma transferência de valor superior ao saldo disponível na conta de origem.
     *
     * Cenário:
     * - A conta de origem tem um saldo de 50.
     * - Tenta transferir o valor de 100 para a conta de destino.
     *
     * Exceção Esperada:
     * - SaldoInsuficienteException.
     */
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

    /**
     * Testa o método de transferência entre contas inativas.
     *
     * Este teste verifica se a tentativa de transferência de um valor de uma conta
     * marcada como inativa para uma conta ativa lança a exceção OperacaoInvalidaException.
     *
     * O teste garante que:
     * - A transação é iniciada antes da operação.
     * - Uma exceção do tipo OperacaoInvalidaException é lançada ao tentar a transferência.
     **/
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