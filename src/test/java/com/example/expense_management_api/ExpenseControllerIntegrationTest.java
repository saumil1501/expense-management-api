package com.example.expense_management_api;

import com.example.expense_management_api.entity.Category;
import com.example.expense_management_api.entity.User;
import com.example.expense_management_api.repository.CategoryRepository;
import com.example.expense_management_api.repository.ExpenseRepository;
import com.example.expense_management_api.repository.UserRepository;
import com.example.expense_management_api.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private JwtService jwtService;

    private User john;
    private User mary;

    private String johnToken;
    private String maryToken;

    private Category food;
    private Category transport;

    @BeforeEach
    void setUp() {

        expenseRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        john = User.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password")
                .build();

        mary = User.builder()
                .name("Mary Smith")
                .email("mary@example.com")
                .password("password")
                .build();

        john = userRepository.save(john);
        mary = userRepository.save(mary);

        johnToken = jwtService.generateToken(john);
        maryToken = jwtService.generateToken(mary);

        food = categoryRepository.save(
                Category.builder()
                        .name("Food")
                        .build()
        );

        transport = categoryRepository.save(
                Category.builder()
                        .name("Transport")
                        .build()
        );
    }
    
    @Test
    void shouldCreateExpenseSuccessfully() throws Exception {

        String body = """
                {
                    "title": "Lunch",
                    "amount": 250.00,
                    "description": "College lunch",
                    "categoryId": %d,
                    "expenseDate": "2026-10-01"
                }
                """.formatted(food.getId());

        mockMvc.perform(
                post("/api/expenses")
                        .header("Authorization", "Bearer " + johnToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Lunch"))
        .andExpect(jsonPath("$.amount").value(250.00))
        .andExpect(jsonPath("$.categoryName").value("Food"));
    }
    
    @Test
    void shouldRejectInvalidExpenseAmount() throws Exception {

        String body = """
                {
                    "title": "Invalid",
                    "amount": -100,
                    "categoryId": %d,
                    "expenseDate": "2026-10-01"
                }
                """.formatted(food.getId());

        mockMvc.perform(
                post("/api/expenses")
                        .header("Authorization", "Bearer " + johnToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldRejectUnknownCategory() throws Exception {

        String body = """
                {
                    "title": "Invalid Category",
                    "amount": 100,
                    "categoryId": 999999,
                    "expenseDate": "2026-10-01"
                }
                """;

        mockMvc.perform(
                post("/api/expenses")
                        .header("Authorization", "Bearer " + johnToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isNotFound());
    }
    
    private Long createExpense(
            String token,
            String title,
            double amount,
            Long categoryId
    ) throws Exception {

        String body = """
                {
                    "title": "%s",
                    "amount": %s,
                    "description": "Test expense",
                    "categoryId": %d,
                    "expenseDate": "2026-10-01"
                }
                """.formatted(
                    title,
                    amount,
                    categoryId
                );

        String response = mockMvc.perform(
                post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

        String idValue = response
                .replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1");

        return Long.parseLong(idValue);
    }
    
    @Test
    void shouldGetOwnExpense() throws Exception {

        Long expenseId = createExpense(
                johnToken,
                "Lunch",
                250,
                food.getId()
        );

        mockMvc.perform(
                get("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + johnToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Lunch"));
    }
    
    @Test
    void shouldNotAllowMaryToReadJohnsExpense() throws Exception {

        Long expenseId = createExpense(
                johnToken,
                "John Expense",
                300,
                food.getId()
        );

        mockMvc.perform(
                get("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + maryToken)
        )
        .andExpect(status().isNotFound());
    }
    
    
    @Test
    void shouldNotAllowMaryToUpdateJohnsExpense() throws Exception {

        Long expenseId = createExpense(
                johnToken,
                "John Expense",
                300,
                food.getId()
        );

        String body = """
                {
                    "title": "Hacked",
                    "amount": 9999,
                    "description": "Unauthorized update",
                    "categoryId": %d,
                    "expenseDate": "2026-10-02"
                }
                """.formatted(food.getId());

        mockMvc.perform(
                put("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + maryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isNotFound());
    }
    
    @Test
    void shouldNotAllowMaryToDeleteJohnsExpense() throws Exception {

        Long expenseId = createExpense(
                johnToken,
                "John Expense",
                300,
                food.getId()
        );

        mockMvc.perform(
                delete("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + maryToken)
        )
        .andExpect(status().isNotFound());
    }
    
    @Test
    void shouldUpdateOwnExpense() throws Exception {

        Long expenseId = createExpense(
                johnToken,
                "Lunch",
                250,
                food.getId()
        );

        String body = """
                {
                    "title": "Updated Lunch",
                    "amount": 300,
                    "description": "Updated",
                    "categoryId": %d,
                    "expenseDate": "2026-10-02"
                }
                """.formatted(food.getId());

        mockMvc.perform(
                put("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + johnToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title")
                .value("Updated Lunch"))
        .andExpect(jsonPath("$.amount")
                .value(300));
    }
    
    @Test
    void shouldDeleteOwnExpense() throws Exception {

        Long expenseId = createExpense(
                johnToken,
                "Lunch",
                250,
                food.getId()
        );

        mockMvc.perform(
                delete("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + johnToken)
        )
        .andExpect(status().isNoContent());

        mockMvc.perform(
                get("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + johnToken)
        )
        .andExpect(status().isNotFound());
    }
    
    
    @Test
    void shouldOnlyReturnCurrentUsersExpenses() throws Exception {

        createExpense(
                johnToken,
                "John Lunch",
                250,
                food.getId()
        );

        createExpense(
                maryToken,
                "Mary Taxi",
                500,
                transport.getId()
        );

        mockMvc.perform(
                get("/api/expenses")
                        .header("Authorization", "Bearer " + johnToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].title")
                .value("John Lunch"));
    }
    
    @Test
    void shouldFilterByCategory() throws Exception {

        createExpense(
                johnToken,
                "Lunch",
                250,
                food.getId()
        );

        createExpense(
                johnToken,
                "Taxi",
                500,
                transport.getId()
        );

        mockMvc.perform(
                get("/api/expenses")
                        .param("category", "Food")
                        .header("Authorization", "Bearer " + johnToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].title")
                .value("Lunch"));
    }
    
    
    @Test
    void shouldPaginateExpenses() throws Exception {

        createExpense(johnToken, "Expense 1", 100, food.getId());
        createExpense(johnToken, "Expense 2", 200, food.getId());
        createExpense(johnToken, "Expense 3", 300, food.getId());

        mockMvc.perform(
                get("/api/expenses")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + johnToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.totalPages").value(2));
    }
    
    
    @Test
    void shouldSortExpensesByAmountAscending() throws Exception {

        createExpense(johnToken, "High", 900, food.getId());
        createExpense(johnToken, "Low", 100, food.getId());
        createExpense(johnToken, "Medium", 500, food.getId());

        mockMvc.perform(
                get("/api/expenses")
                        .param("sortBy", "amount")
                        .param("direction", "asc")
                        .header("Authorization", "Bearer " + johnToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].amount").value(100))
        .andExpect(jsonPath("$.content[1].amount").value(500))
        .andExpect(jsonPath("$.content[2].amount").value(900));
    }
}