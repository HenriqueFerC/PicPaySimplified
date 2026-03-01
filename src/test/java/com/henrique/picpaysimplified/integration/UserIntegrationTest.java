package com.henrique.picpaysimplified.integration;

import com.henrique.picpaysimplified.dtos.userDto.RegisterUserDto;
import com.henrique.picpaysimplified.model.TypeUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should register a new user with successfully")
    void testUserRegisterEndPoint() throws Exception{
        mockMvc.perform(post("/user/register")
                .contentType("application/json")
                .content("""
                        {
                            "fullName": "Henrique Ferreira",
                            "cpfCnpj": "123.456.789-10",
                            "email": "henrique@gmail.com",
                            "password": "password123",
                            "typeUser": "user"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cpfCnpj").value("123.456.789-10"))
                .andExpect(jsonPath("$.email").value("henrique@gmail.com"));
    }
}
