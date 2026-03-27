package com.henrique.picpaysimplified.integration;

import com.henrique.picpaysimplified.repository.BankAccountRepository;
import com.henrique.picpaysimplified.repository.TransactionRepository;
import com.henrique.picpaysimplified.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
public class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withReuse(false)
            .waitingFor(Wait.forListeningPort()
                    .withStartupTimeout(Duration.ofMinutes(5)));

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected Integer registerUserAndGetId(String fullName, String cpfCnpj, String email, String password, String userType) throws Exception {
        String response = mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "%s",
                                    "cpfCnpj": "%s",
                                    "email": "%s",
                                    "password": "%s",
                                    "userType": "%s"
                                }
                                """.formatted(fullName, cpfCnpj, email, password, userType)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.id");
    }

    protected String authenticate(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.token");
    }

    protected String createUserAndAuthenticate(String fullName, String cpfCnpj, String email, String password, String userType) throws Exception {
        registerUserAndGetId(fullName, cpfCnpj, email, password, userType);
        return authenticate(email, password);
    }

    protected void registerBankAccount(String token, Integer agency, Integer accountNumber, BigDecimal balance) throws Exception {
        mockMvc.perform(post("/bankAccount/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "agency": %d,
                                    "accountNumber": %d,
                                    "balance": %s
                                }
                                """.formatted(agency, accountNumber, balance.toPlainString())))
                .andExpect(status().isCreated());
    }

}
