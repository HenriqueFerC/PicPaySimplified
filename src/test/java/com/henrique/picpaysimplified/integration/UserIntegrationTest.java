package com.henrique.picpaysimplified.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class UserIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should register a new user successfully")
    void testUserRegisterEndPoint() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Henrique Ferreira",
                                    "cpfCnpj": "123.456.789-10",
                                    "email": "henrique@gmail.com",
                                    "password": "password123",
                                    "userType": "user"
                                }
                                """))
                .andExpectAll(status().isCreated(),
                        jsonPath("$.id").exists(),
                        jsonPath("$.fullName").value("Henrique Ferreira"),
                        jsonPath("$.cpfCnpj").value("123.456.789-10"),
                        jsonPath("$.email").value("henrique@gmail.com"),
                        jsonPath("$.userType").value("user"));
    }

    @Test
    @DisplayName("Should return error when trying to register a user with an email that already exists")
    void testUserRegisterEndPointWithExistingEmail() throws Exception {
        registerUserAndGetId("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Henrique Ferreira",
                                    "cpfCnpj": "123.456.789-10",
                                    "email": "henrique@gmail.com",
                                    "password": "password123",
                                    "userType": "user"
                                }
                                """))
                .andExpectAll(status().isConflict(),
                        jsonPath("$.message").value("Email already exists henrique@gmail.com"));
    }

    @Test
    @DisplayName("Should return error when trying to register a user with a CPF/CNPJ that already exists")
    void testUserRegisterEndPointWithExistingCpfCnpj() throws Exception {
        registerUserAndGetId("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Henrique Ferreira",
                                    "cpfCnpj": "123.456.789-10",
                                    "email": "henrique1@gmail.com",
                                    "password": "password123",
                                    "userType": "user"
                                }
                                """))
                .andExpectAll(status().isConflict(),
                        jsonPath("$.message").value("Cpf or Cnpj already exists 123.456.789-10"));
    }

    @Test
    @DisplayName("Should login user and returns token")
    void testLoginUser() throws Exception {
        registerUserAndGetId("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "henrique@gmail.com",
                                    "password": "password123"
                                }
                                
                                """))
                .andExpectAll(status().isOk(),
                        jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Should return conflict when login with invalid credentials")
    void testLoginUserInvalidCredentials() throws Exception {
        registerUserAndGetId("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "henrique@gmail.com",
                                    "password": "wrong-password"
                                }
                                """))
                .andExpectAll(status().isConflict(),
                        jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    @DisplayName("Should list users successfully")
    void testListUsers() throws Exception {
        registerUserAndGetId("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");
        registerUserAndGetId("Ana Luiza Teste", "987.654.321-01", "analuiza@gmail.com", "password123", "user");

        mockMvc.perform(get("/user/list"))
                .andExpectAll(status().isOk(),
                        jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return my profile successfully")
    void testMyProfileWithAuthorization() throws Exception {
        String token = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");

        mockMvc.perform(get("/user/myProfile")
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(status().isOk(),
                        jsonPath("$.fullName").value("Henrique Ferreira"),
                        jsonPath("$.cpfCnpj").value("123.456.789-10"),
                        jsonPath("$.email").value("henrique@gmail.com"),
                        jsonPath("$.userType").value("user"));
    }

    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUserWithSuccessfully() throws Exception {
        String token = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");

        mockMvc.perform(put("/user/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Henrique Ferreira C",
                                    "cpfCnpj": "123.456.789-10",
                                    "email": "henrique@gmail.com",
                                    "password": "password123",
                                    "userType": "user"
                                }
                                """))
                .andExpectAll(status().isOk(),
                        jsonPath("$.fullName").value("Henrique Ferreira C"),
                        jsonPath("$.cpfCnpj").value("123.456.789-10"),
                        jsonPath("$.email").value("henrique@gmail.com"),
                        jsonPath("$.userType").value("user"));
    }

    @Test
    @DisplayName("Should not update user and return error when trying to update with a cpf or cnpj that already exists")
    void testUpdateUserCpfCnpjException() throws Exception {
        String token = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");

        registerUserAndGetId("Ana Luiza", "987.654.321-01", "analuiza@gmail.com", "password123", "user");

        mockMvc.perform(put("/user/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Henrique Ferreira C",
                                    "cpfCnpj": "987.654.321-01",
                                    "email": "henrique@gmail.com",
                                    "password": "password123",
                                    "userType": "user"
                                }
                                """))
                .andExpectAll(status().isConflict(),
                        jsonPath("$.message").value("Cpf or Cnpj already exists 987.654.321-01"));
    }

    @Test
    @DisplayName("Should not update user and return error when trying to update with an email that already exists")
    void testUpdateUserEmailException() throws Exception {
        String token = createUserAndAuthenticate("Henrique Ferreira", "123.456.789-10", "henrique@gmail.com", "password123", "user");

        registerUserAndGetId("Ana Luiza", "987.654.321-01", "analuiza@gmail.com", "password123", "user");

        mockMvc.perform(put("/user/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Henrique Ferreira C",
                                    "cpfCnpj": "123.456.789-10",
                                    "email": "analuiza@gmail.com",
                                    "password": "password123",
                                    "userType": "user"
                                }
                                """))
                .andExpectAll(status().isConflict(),
                        jsonPath("$.message").value("Email already exists analuiza@gmail.com"));
    }

}
