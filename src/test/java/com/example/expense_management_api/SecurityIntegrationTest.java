package com.example.expense_management_api;

import com.example.expense_management_api.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.expense_management_api.entity.User;
import com.example.expense_management_api.security.JwtService;


import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;
    

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }
    
    @Test
    void shouldRejectRequestWithoutJwt() throws Exception {

        mockMvc.perform(
                get("/api/expenses")
        )
        .andExpect(status().isUnauthorized());
    }
    
    @Test
    void shouldRejectInvalidJwt() throws Exception {

        mockMvc.perform(
                get("/api/expenses")
                        .header(
                            "Authorization",
                            "Bearer this-is-not-a-valid-jwt"
                        )
        )
        .andExpect(status().isUnauthorized());
    }
    
    @Test
    void shouldAllowRequestWithValidJwt() throws Exception {

        String registerBody = """
                {
                    "name": "John Doe",
                    "email": "john@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody)
        )
        .andExpect(status().isCreated());

        User user = userRepository
                .findByEmailIgnoreCase("john@example.com")
                .orElseThrow();

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                get("/api/expenses")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk());
    }
}