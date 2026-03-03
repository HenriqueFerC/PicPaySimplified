package com.henrique.picpaysimplified.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class BankAccountIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should register bank account successfully")
    void testRegisterBankAccountSuccessfully() throws Exception {
        String token = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");

        mockMvc.perform(post("/bankAccount/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "agency": 1234,
                                    "accountNumber": 123456,
                                    "balance": 500.00
                                }
                                """))
                .andExpectAll(status().isCreated(),
                        jsonPath("$.agency").value(1234),
                        jsonPath("$.accountNumber").value(123456),
                        jsonPath("$.value").value(500.00));
    }

    @Test
    @DisplayName("Should return forbidden when trying to register bank account without authorization")
    void testRegisterBankAccountWithoutAuthorization() throws Exception {
        mockMvc.perform(post("/bankAccount/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "agency": 1234,
                                    "accountNumber": 123456,
                                    "balance": 500.00
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return conflict when trying to register duplicated agency")
    void testRegisterBankAccountWithDuplicatedAgency() throws Exception {
        String firstToken = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(firstToken, 1234, 123456, new BigDecimal("500.00"));

        String secondToken = createUserAndAuthenticate("Ana Luiza Teste", "987.654.321-01", "analuiza@gmail.com", "password123", "user");

        mockMvc.perform(post("/bankAccount/register")
                        .header("Authorization", "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "agency": 1234,
                                    "accountNumber": 654321,
                                    "balance": 300.00
                                }
                                """))
                .andExpectAll(status().isConflict(),
                        jsonPath("$.message").value("Agency already exists 1234"));
    }

    @Test
    @DisplayName("Should deposit successfully")
    void testDepositSuccessfully() throws Exception {
        String token = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(token, 1234, 123456, new BigDecimal("500.00"));

        mockMvc.perform(post("/bankAccount/deposit")
                        .param("amount", "100.00")
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(status().isOk(),
                        jsonPath("$.value").value(600.00));
    }

    @Test
    @DisplayName("Should return forbidden when trying to deposit without authorization")
    void testDepositWithoutAuthorization() throws Exception {
        mockMvc.perform(post("/bankAccount/deposit")
                        .param("amount", "100.00"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should withdraw successfully")
    void testWithdrawSuccessfully() throws Exception {
        String token = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(token, 1234, 123456, new BigDecimal("500.00"));

        mockMvc.perform(post("/bankAccount/withdraw")
                        .param("amount", "100.00")
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(status().isOk(),
                        jsonPath("$.value").value(400.00));
    }

    @Test
    @DisplayName("Should return forbidden when trying to withdraw without authorization")
    void testWithdrawWithoutAuthorization() throws Exception {
        mockMvc.perform(post("/bankAccount/withdraw")
                        .param("amount", "100.00"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return conflict when trying to withdraw with insufficient balance")
    void testWithdrawInsufficientBalance() throws Exception {
        String token = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(token, 1234, 123456, new BigDecimal("50.00"));

        mockMvc.perform(post("/bankAccount/withdraw")
                        .param("amount", "100.00")
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(status().isConflict(),
                        jsonPath("$.message").value("Insufficient balance: 50.00"));
    }
}
