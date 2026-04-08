package com.henrique.picpaysimplified.integration;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BankAccountIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should register bank account successfully")
    void testRegisterBankAccountSuccessfully() throws Exception {
        Cookie cookie = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");

        mockMvc.perform(post("/bankAccount/register")
                        .cookie(cookie)
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
                        jsonPath("$.balance").value(500.00));
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
        Cookie firstCookie = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(firstCookie, 1234, 123456, new BigDecimal("500.00"));

        Cookie secondCookie = createUserAndAuthenticate("Ana Luiza Teste", "987.654.321-01", "analuiza@gmail.com", "password123", "user");

        mockMvc.perform(post("/bankAccount/register")
                        .cookie(secondCookie)
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
    @DisplayName("Should return conflict when trying to register duplicated account number")
    void testRegisterBankAccountWithDuplicatedAccountNumber() throws Exception {
        Cookie firstCookie = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(firstCookie, 1234, 654321, new BigDecimal("500.00"));

        Cookie secondCookie = createUserAndAuthenticate("Ana Luiza Teste", "987.654.321-01", "analuiza@gmail.com", "password123", "user");

        mockMvc.perform(post("/bankAccount/register")
                        .cookie(secondCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "agency": 1235,
                                    "accountNumber": 654321,
                                    "balance": 300.00
                                }
                                """))
                .andExpectAll(status().isConflict(),
                        jsonPath("$.message").value("Account Number already exists 654321"));
    }

    @Test
    @DisplayName("Should return your bank account details successfully")
    void testMyBankAccountWithAuthorization() throws Exception {
        Cookie cookie = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerBankAccount(cookie, 1234, 654321, new BigDecimal("500.00"));

        mockMvc.perform(get("/bankAccount/myBankAccount")
                        .cookie(cookie))
                .andExpectAll(status().isOk(),
                        jsonPath("$.userDto.fullName").value("Henrique Ferreira"),
                        jsonPath("$.userDto.cpfCnpj").value("123.456.789-10"),
                        jsonPath("$.userDto.email").value("henrique@gmail.com"),
                        jsonPath("$.agency").value(1234));
    }

}
