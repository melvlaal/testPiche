package com.example.picheTest;

import com.example.picheTest.model.request.AccountCreateRQ;
import com.example.picheTest.model.request.DepositRQ;
import com.example.picheTest.model.request.TransferRQ;
import com.example.picheTest.model.request.WithdrawRQ;
import com.example.picheTest.repository.entity.Account;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BaseTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private static final String BASE_URL = "http://localhost:%s/accounts";
    private static final String ACCOUNT_NUMBER = "1111-1111-1111-1111";
    private static final String SECOND_ACCOUNT_NUMBER = "1111-1111-1111-1112";
    static final PostgreSQLContainer<?> postgreSQLContainer;
    @Autowired
    private Environment environment;

    static {
        postgreSQLContainer =
                new PostgreSQLContainer<>(
                        DockerImageName.parse(PostgreSQLContainer.IMAGE + ":15.3-bullseye"))
                        .withDatabaseName("testdb")
                        .withPassword("postgres")
                        .withUsername("postgres");
        postgreSQLContainer.start();
    }

    @BeforeAll
    public static void beforeAll() {
        runSql(new String(readAsBytes("db/schema.sql")));
        System.out.println(postgreSQLContainer.getLogs());
    }


    @AfterEach
    public void afterEach() {
        runSql(new String(readAsBytes("db/clean.sql")));
    }


    @DynamicPropertySource
    static void datasourceConfig(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @SneakyThrows
    protected static byte[] readAsBytes(String path) {
        return new ClassPathResource(path).getInputStream().readAllBytes();
    }

    private static void runSql(String queries) {
        String url = postgreSQLContainer.getJdbcUrl();
        String username = postgreSQLContainer.getUsername();
        String password = postgreSQLContainer.getPassword();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(queries);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ResponseEntity<Account> createAccount(String accountNumber, BigDecimal balance){
        AccountCreateRQ request = new AccountCreateRQ(accountNumber, balance);

        return restTemplate.postForEntity(BASE_URL.formatted(port), request, Account.class);
    }

    @Test
    void testCreateAccount() {
        ResponseEntity<Account> response = this.createAccount(ACCOUNT_NUMBER, BigDecimal.valueOf(1000));

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(ACCOUNT_NUMBER, response.getBody().getAccountNumber());
        assertEquals(BigDecimal.valueOf(1000), response.getBody().getBalance());
    }

    @Test
    void testCreateAccountWithSameAccountNumber() {
        this.createAccount(ACCOUNT_NUMBER, BigDecimal.valueOf(1000));

        AccountCreateRQ request = new AccountCreateRQ(ACCOUNT_NUMBER, BigDecimal.valueOf(1000));
        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL.formatted(port), request, String.class);

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Account number already exists: " + ACCOUNT_NUMBER, response.getBody());
    }

    @Test
    void testGetAccount() {
        this.createAccount(ACCOUNT_NUMBER, BigDecimal.valueOf(1000));

        String firstAccountUrl = BASE_URL.formatted(port) + "/" + ACCOUNT_NUMBER;
        ResponseEntity<Account> response = restTemplate.getForEntity(firstAccountUrl,  Account.class);

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ACCOUNT_NUMBER, response.getBody().getAccountNumber());
        assertEquals(0, response.getBody().getBalance().compareTo(BigDecimal.valueOf(1000)));
    }

    @Test
    void testGetAccountNotFound() {
        String firstAccountUrl = BASE_URL.formatted(port) + "/" + ACCOUNT_NUMBER;
        ResponseEntity<String> response = restTemplate.getForEntity(firstAccountUrl,  String.class);

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account with account number " + ACCOUNT_NUMBER + " not found", response.getBody());
    }

    @Test
    void testGetAllAccounts() {
        this.createAccount(ACCOUNT_NUMBER, BigDecimal.valueOf(1000));
        this.createAccount(SECOND_ACCOUNT_NUMBER, BigDecimal.valueOf(1000));

        ResponseEntity<List<Account>> response = restTemplate.exchange(
                BASE_URL.formatted(port),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Account>>() {}
        );

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected status code 200");
        assertFalse(response.getBody().isEmpty(), "The list of accounts should not be empty");
        assertTrue(response.getBody().stream().anyMatch(account -> account.getAccountNumber().equals(ACCOUNT_NUMBER)),
                "The list of accounts should contain the account with accountNumber " + ACCOUNT_NUMBER);
        assertTrue(response.getBody().stream().anyMatch(account -> account.getAccountNumber().equals(SECOND_ACCOUNT_NUMBER)),
                "The list of accounts should contain the account with accountNumber " + SECOND_ACCOUNT_NUMBER);
    }

    @Test
    void testGetAllAccounts_emptyList() {
        ResponseEntity<List<Account>> response = restTemplate.exchange(
                BASE_URL.formatted(port),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Account>>() {}
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Expected status code 204");
    }

    @Test
    void testDepositFunds() {
        this.createAccount(ACCOUNT_NUMBER, BigDecimal.valueOf(1000));
        DepositRQ deposit = new DepositRQ(BigDecimal.valueOf(500));

        // Отправляем POST запрос для пополнения счета
        String url = BASE_URL.formatted(port) + "/" + ACCOUNT_NUMBER + "/deposit";
        ResponseEntity<Account> response = restTemplate.postForEntity(url, deposit, Account.class);

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ACCOUNT_NUMBER, response.getBody().getAccountNumber());
        assertEquals(0, response.getBody().getBalance().compareTo(BigDecimal.valueOf(1500)));
    }

    @Test
    void testWithdrawFunds() {
        this.createAccount(ACCOUNT_NUMBER, BigDecimal.valueOf(1000));
        WithdrawRQ withdraw = new WithdrawRQ(BigDecimal.valueOf(300));

        String url = BASE_URL.formatted(port) + "/" + ACCOUNT_NUMBER + "/withdraw";
        ResponseEntity<Account> response = restTemplate.postForEntity(url, withdraw, Account.class);

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ACCOUNT_NUMBER, response.getBody().getAccountNumber());
        assertEquals(0, response.getBody().getBalance().compareTo(BigDecimal.valueOf(700)));
    }

    @Test
    void testWithdrawFundsNotHaveSufficientBalance() {
        this.createAccount(ACCOUNT_NUMBER, BigDecimal.valueOf(1000));
        WithdrawRQ withdraw = new WithdrawRQ(BigDecimal.valueOf(1300));

        String url = BASE_URL.formatted(port) + "/" + ACCOUNT_NUMBER + "/withdraw";
        ResponseEntity<String> response = restTemplate.postForEntity(url, withdraw, String.class);

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Account " + ACCOUNT_NUMBER + " does not have sufficient balance", response.getBody());
    }

    @Test
    void testTransferFunds() {
        this.createAccount(ACCOUNT_NUMBER, BigDecimal.valueOf(1000));
        this.createAccount(SECOND_ACCOUNT_NUMBER, BigDecimal.valueOf(500));

        TransferRQ transfer = new TransferRQ(ACCOUNT_NUMBER, SECOND_ACCOUNT_NUMBER, BigDecimal.valueOf(200));

        String transferUrl = BASE_URL.formatted(port) + "/transfer";
        ResponseEntity<Boolean> transferResponse = restTemplate.postForEntity(transferUrl, transfer, Boolean.class);

        assertNotNull(transferResponse.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.OK, transferResponse.getStatusCode());
        assertEquals(true, transferResponse.getBody());

        String firstAccountUrl = BASE_URL.formatted(port) + "/" + ACCOUNT_NUMBER;
        ResponseEntity<Account> firstAccountResponse = restTemplate.getForEntity(firstAccountUrl,  Account.class);

        assertNotNull(firstAccountResponse.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.OK, firstAccountResponse.getStatusCode());
        assertEquals(ACCOUNT_NUMBER, firstAccountResponse.getBody().getAccountNumber());
        assertEquals(0, firstAccountResponse.getBody().getBalance().compareTo(BigDecimal.valueOf(800)));


        String secondAccountUrl = BASE_URL.formatted(port) + "/" + SECOND_ACCOUNT_NUMBER;
        ResponseEntity<Account> secondAccountResponse = restTemplate.getForEntity(secondAccountUrl,  Account.class);

        assertNotNull(secondAccountResponse.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.OK, secondAccountResponse.getStatusCode());
        assertEquals(SECOND_ACCOUNT_NUMBER, secondAccountResponse.getBody().getAccountNumber());
        assertEquals(0, secondAccountResponse.getBody().getBalance().compareTo(BigDecimal.valueOf(700)));
    }

    @Test
    void testTransferFundsNotHaveSufficientBalance() {
        this.createAccount(ACCOUNT_NUMBER, BigDecimal.valueOf(1000));
        this.createAccount(SECOND_ACCOUNT_NUMBER, BigDecimal.valueOf(500));

        TransferRQ transfer = new TransferRQ(ACCOUNT_NUMBER, SECOND_ACCOUNT_NUMBER, BigDecimal.valueOf(1200));

        String transferUrl = BASE_URL.formatted(port) + "/transfer";
        ResponseEntity<String> transferResponse = restTemplate.postForEntity(transferUrl, transfer, String.class);

        assertNotNull(transferResponse.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, transferResponse.getStatusCode());
        assertEquals("Account " + ACCOUNT_NUMBER + " does not have sufficient balance", transferResponse.getBody());
    }
}
