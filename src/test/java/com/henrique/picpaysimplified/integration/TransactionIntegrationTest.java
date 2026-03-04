package com.henrique.picpaysimplified.integration;

import com.henrique.picpaysimplified.service.RestTemplateService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class TransactionIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private RestTemplateService restTemplateService;

    @Test
    @DisplayName("Should create transaction successfully")
    void testCreateTransactionSuccessfully() throws Exception {
        when(restTemplateService.authorizeTransaction()).thenReturn(true);

        Integer payerId = registerUserAndGetId("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        String payerToken = authenticate("henrique@gmail.com", "password123");
        registerBankAccount(payerToken, 1234, 123456, new BigDecimal("1000.00"));

        Integer payeeId = registerUserAndGetId("Ana Luiza Teste", "987.654.321-01", "analuiza@gmail.com", "password123", "user");
        String payeeToken = authenticate("analuiza@gmail.com", "password123");
        registerBankAccount(payeeToken, 4321, 654321, new BigDecimal("100.00"));

        mockMvc.perform(post("/transaction")
                        .header("Authorization", "Bearer " + payerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "value": 150.00,
                                    "idPayee": %d
                                }
                                """.formatted(payeeId)))
                .andExpectAll(status().isCreated(),
                        jsonPath("$.id").exists(),
                        jsonPath("$.value").value(150.00),
                        jsonPath("$.idPayer").value(payerId),
                        jsonPath("$.idPayee").value(payeeId),
                        jsonPath("$.consistency").value("completed"));
    }

    @Test
    @DisplayName("Should return forbidden when trying to create transaction without authorization")
    void testCreateTransactionWithoutAuthorization() throws Exception {
        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "value": 150.00,
                                    "idPayee": 2
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return conflict when trying to create transaction with insufficient balance")
    void testCreateTransactionWithInsufficientBalance() throws Exception {
        when(restTemplateService.authorizeTransaction()).thenReturn(true);

        String payerToken = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(payerToken, 1234, 123456, new BigDecimal("50.00"));

        Integer payeeId = registerUserAndGetId("Ana Luiza Teste", "987.654.321-01", "analuiza@gmail.com", "password123", "user");
        String payeeToken = authenticate("analuiza@gmail.com", "password123");
        registerBankAccount(payeeToken, 4321, 654321, new BigDecimal("100.00"));

        mockMvc.perform(post("/transaction")
                        .header("Authorization", "Bearer " + payerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "value": 100.00,
                                    "idPayee": %d
                                }
                                """.formatted(payeeId)))
                .andExpectAll(status().isConflict(),
                        jsonPath("$.message").value("Insufficient balance: 50.00"));
    }

    @Test
    @DisplayName("Should list my transactions successfully")
    void testListMyTransactionsSuccessfully() throws Exception {
        Integer payeeId = prepareTransactionAndReturnPayeeId();
        String payerToken = authenticate("henrique@gmail.com", "password123");

        mockMvc.perform(post("/transaction")
                        .header("Authorization", "Bearer " + payerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "value": 100.00,
                                    "idPayee": %d
                                }
                                """.formatted(payeeId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/transaction/myTransactions")
                        .header("Authorization", "Bearer " + payerToken))
                .andExpectAll(status().isOk(),
                        jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should return forbidden when trying to list my transactions without authorization")
    void testListMyTransactionsWithoutAuthorization() throws Exception {
        mockMvc.perform(get("/transaction/myTransactions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should list last transactions successfully")
    void testListLastTransactionsSuccessfully() throws Exception {
        Integer payeeId = prepareTransactionAndReturnPayeeId();
        String payerToken = authenticate("henrique@gmail.com", "password123");

        mockMvc.perform(post("/transaction")
                        .header("Authorization", "Bearer " + payerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "value": 100.00,
                                    "idPayee": %d
                                }
                                """.formatted(payeeId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/transaction/lastTransactions")
                        .param("days", "1")
                        .header("Authorization", "Bearer " + payerToken))
                .andExpectAll(status().isOk(),
                        jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should return forbidden when trying to list last transactions without authorization")
    void testListLastTransactionsWithoutAuthorization() throws Exception {
        mockMvc.perform(get("/transaction/lastTransactions")
                        .param("days", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should revert transaction successfully")
    void testRevertTransactionSuccessfully() throws Exception {
        Integer payeeId = prepareTransactionAndReturnPayeeId();
        String payerToken = authenticate("henrique@gmail.com", "password123");

        String response = mockMvc.perform(post("/transaction")
                        .header("Authorization", "Bearer " + payerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "value": 100.00,
                                    "idPayee": %d
                                }
                                """.formatted(payeeId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer transactionId = JsonPath.read(response, "$.id");

        mockMvc.perform(put("/transaction/revert/{id}", transactionId)
                        .header("Authorization", "Bearer " + payerToken))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id").value(transactionId),
                        jsonPath("$.consistency").value("reverted"));
    }

    @Test
    @DisplayName("Should return forbidden when trying to revert transaction without authorization")
    void testRevertTransactionWithoutAuthorization() throws Exception {
        mockMvc.perform(put("/transaction/revert/{id}", 1))
                .andExpect(status().isForbidden());
    }

    private Integer prepareTransactionAndReturnPayeeId() throws Exception {
        when(restTemplateService.authorizeTransaction()).thenReturn(true);

        String payerToken = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(payerToken, 1234, 123456, new BigDecimal("1000.00"));

        Integer payeeId = registerUserAndGetId("Ana Luiza Teste", "987.654.321-01", "analuiza@gmail.com", "password123", "user");
        String payeeToken = authenticate("analuiza@gmail.com", "password123");
        registerBankAccount(payeeToken, 4321, 654321, new BigDecimal("100.00"));
        return payeeId;
    }
}
