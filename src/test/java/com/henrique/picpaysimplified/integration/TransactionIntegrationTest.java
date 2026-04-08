package com.henrique.picpaysimplified.integration;

import com.henrique.picpaysimplified.service.RestTemplateService;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TransactionIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private RestTemplateService restTemplateService;

    @Test
    @DisplayName("Should create transaction successfully")
    void testCreateTransactionSuccessfully() throws Exception {
        when(restTemplateService.authorizeTransaction()).thenReturn(true);

        Integer payerId = registerUserAndGetId("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        Cookie payerCookie = authenticate("henrique@gmail.com", "password123");
        registerBankAccount(payerCookie, 1234, 123456, new BigDecimal("1000.00"));

        Integer payeeId = registerUserAndGetId("Ana Luiza Teste", "987.654.321-01", "analuiza@gmail.com", "password123", "user");
        Cookie payeeCookie = authenticate("analuiza@gmail.com", "password123");
        registerBankAccount(payeeCookie, 4321, 654321, new BigDecimal("100.00"));

        mockMvc.perform(post("/transaction").cookie(payerCookie).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "value": 150.00,
                    "idPayee": %d,
                    "transactionType": "transfer"
                }
                """.formatted(payeeId))).andExpectAll(status().isCreated(), jsonPath("$.id").exists(), jsonPath("$.value").value(150.00), jsonPath("$.payer.id").value(payerId), jsonPath("$.payee.id").value(payeeId), jsonPath("$.transactionType").value("transfer"), jsonPath("$.consistency").value("completed"));
    }

    @Test
    @DisplayName("Should return forbidden when trying to create transaction without authorization")
    void testCreateTransactionWithoutAuthorization() throws Exception {
        mockMvc.perform(post("/transaction").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "value": 150.00,
                    "idPayee": 2,
                    "transferType": "transfer"
                }
                """)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return conflict when trying to create transaction with insufficient balance")
    void testCreateTransactionWithInsufficientBalance() throws Exception {
        when(restTemplateService.authorizeTransaction()).thenReturn(true);

        Cookie payerCookie = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(payerCookie, 1234, 123456, new BigDecimal("50.00"));

        Integer payeeId = registerUserAndGetId("Ana Luiza Teste", "987.654.321-01", "analuiza@gmail.com", "password123", "user");
        Cookie payeeCookie = authenticate("analuiza@gmail.com", "password123");
        registerBankAccount(payeeCookie, 4321, 654321, new BigDecimal("100.00"));

        mockMvc.perform(post("/transaction").cookie(payerCookie).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "value": 100.00,
                    "idPayee": %d,
                    "transactionType": "transfer"
                }
                """.formatted(payeeId))).andExpectAll(status().isConflict(), jsonPath("$.message").value("Insufficient balance: 50.00"));
    }

    @Test
    @DisplayName("Should list my transactions successfully")
    void testListMyTransactionsSuccessfully() throws Exception {
        Integer payeeId = prepareTransactionAndReturnPayeeId();
        Cookie payerCookie = authenticate("henrique@gmail.com", "password123");

        mockMvc.perform(post("/transaction").cookie(payerCookie).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "value": 100.00,
                    "idPayee": %d,
                    "transactionType": "transfer"
                }
                """.formatted(payeeId))).andExpect(status().isCreated());

        mockMvc.perform(get("/transaction/myTransactions").cookie(payerCookie)).andExpectAll(status().isOk(), jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should return forbidden when trying to list my transactions without authorization")
    void testListMyTransactionsWithoutAuthorization() throws Exception {
        mockMvc.perform(get("/transaction/myTransactions")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should list last transactions successfully")
    void testListLastTransactionsSuccessfully() throws Exception {
        Integer payeeId = prepareTransactionAndReturnPayeeId();
        Cookie payerCookie = authenticate("henrique@gmail.com", "password123");

        mockMvc.perform(post("/transaction").cookie(payerCookie).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "value": 100.00,
                    "idPayee": %d,
                    "transactionType": "transfer"
                }
                """.formatted(payeeId))).andExpect(status().isCreated());

        mockMvc.perform(get("/transaction/lastTransactions").param("days", "1").cookie(payerCookie)).andExpectAll(status().isOk(), jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should return forbidden when trying to list last transactions without authorization")
    void testListLastTransactionsWithoutAuthorization() throws Exception {
        mockMvc.perform(get("/transaction/lastTransactions").param("days", "1")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should revert transaction successfully")
    void testRevertTransactionSuccessfully() throws Exception {
        Integer payeeId = prepareTransactionAndReturnPayeeId();
        Cookie payerCookie = authenticate("henrique@gmail.com", "password123");

        String response = mockMvc.perform(post("/transaction").cookie(payerCookie).contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "value": 100.00,
                    "idPayee": %d,
                    "transactionType": "transfer"
                }
                """.formatted(payeeId))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        Integer transactionId = JsonPath.read(response, "$.id");

        mockMvc.perform(put("/transaction/revert/{id}", transactionId).cookie(payerCookie)).andExpectAll(status().isOk(), jsonPath("$.id").value(transactionId), jsonPath("$.consistency").value("reverted"));
    }

    @Test
    @DisplayName("Should return forbidden when trying to revert transaction without authorization")
    void testRevertTransactionWithoutAuthorization() throws Exception {
        mockMvc.perform(put("/transaction/revert/{id}", 1)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should deposit successfully")
    void testDepositSuccessfully() throws Exception {
        Cookie cookie = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(cookie, 1234, 123456, new BigDecimal("500.00"));

        mockMvc.perform(post("/transaction/deposit").param("amount", "100.00").cookie(cookie)).andExpectAll(status().isOk(), jsonPath("$.value").value(100.00), jsonPath("$.transactionType").value("deposit"));
    }

    @Test
    @DisplayName("Should return forbidden when trying to deposit without authorization")
    void testDepositWithoutAuthorization() throws Exception {
        mockMvc.perform(post("/bankAccount/deposit").param("amount", "100.00")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should withdraw successfully")
    void testWithdrawSuccessfully() throws Exception {
        Cookie cookie = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(cookie, 1234, 123456, new BigDecimal("500.00"));

        mockMvc.perform(post("/transaction/withdraw").param("amount", "100.00").cookie(cookie)).andExpectAll(status().isOk(), jsonPath("$.value").value(100.00), jsonPath("$.transactionType").value("withdraw"));
    }

    @Test
    @DisplayName("Should return forbidden when trying to withdraw without authorization")
    void testWithdrawWithoutAuthorization() throws Exception {
        mockMvc.perform(post("/bankAccount/withdraw").param("amount", "100.00")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return conflict when trying to withdraw with insufficient balance")
    void testWithdrawInsufficientBalance() throws Exception {
        Cookie cookie = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(cookie, 1234, 123456, new BigDecimal("50.00"));

        mockMvc.perform(post("/transaction/withdraw")
                        .param("amount", "100.00")
                        .cookie(cookie))
                .andExpectAll(status().isConflict(),
                        jsonPath("$.message").value("Insufficient balance: 50.00"));
    }

    private Integer prepareTransactionAndReturnPayeeId() throws Exception {
        when(restTemplateService.authorizeTransaction()).thenReturn(true);

        Cookie payerCookie = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(payerCookie, 1234, 123456, new BigDecimal("1000.00"));

        Integer payeeId = registerUserAndGetId("Ana Luiza Teste", "987.654.321-01", "analuiza@gmail.com", "password123", "user");
        Cookie payeeCookie = authenticate("analuiza@gmail.com", "password123");
        registerBankAccount(payeeCookie, 4321, 654321, new BigDecimal("100.00"));
        return payeeId;
    }
}
