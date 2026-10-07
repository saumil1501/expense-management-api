package com.example.expense_management_api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.expense_management_api.entity.Category;
import com.example.expense_management_api.entity.Expense;
import com.example.expense_management_api.entity.User;
import com.example.expense_management_api.repository.CategoryRepository;
import com.example.expense_management_api.repository.ExpenseRepository;
import com.example.expense_management_api.repository.UserRepository;
import com.example.expense_management_api.security.JwtService;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseApiIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired CategoryRepository categories;
    @Autowired ExpenseRepository expenses;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtService jwt;
    @Value("${jwt.secret}") String signingSecret;

    User alice;
    User bob;
    Category food;
    String aliceToken;
    String bobToken;

    @BeforeEach
    void prepareData() {
        expenses.deleteAll();
        categories.deleteAll();
        users.deleteAll();
        alice = users.save(User.builder().name("Alice").email("alice@example.com")
                .password(passwords.encode("Password123!")).build());
        bob = users.save(User.builder().name("Bob").email("bob@example.com")
                .password(passwords.encode("Password123!")).build());
        food = categories.save(Category.builder().name("Food").build());
        aliceToken = jwt.generateToken(alice);
        bobToken = jwt.generateToken(bob);
    }

    @Test
    void creationUsesAuthenticatedOwnerAndDoesNotExposePassword() throws Exception {
        String body = json.writeValueAsString(Map.of("title", "Lunch", "amount", 250,
                "categoryId", food.getId(), "expenseDate", "2026-10-07", "userId", bob.getId()));
        String response = mvc.perform(post("/api/expenses").header("Authorization", bearer(aliceToken))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.categoryName").value("Food"))
                .andExpect(jsonPath("$.user").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(response).get("id").asLong();
        assertThat(expenses.findByIdAndUserId(id, alice.getId())).isPresent();
        assertThat(expenses.findByIdAndUserId(id, bob.getId())).isEmpty();
    }

    @Test
    void paginationAndTotalsOnlyIncludeCurrentUsersExpenses() throws Exception {
        expense(alice, "A1", "2026-10-01", "100");
        expense(alice, "A2", "2026-10-02", "200");
        expense(bob, "B1", "2026-10-03", "300");
        expense(bob, "B2", "2026-10-04", "400");
        expense(bob, "B3", "2026-10-05", "500");
        mvc.perform(get("/api/expenses").header("Authorization", bearer(aliceToken))
                .param("size", "1").param("sortBy", "amount").param("direction", "desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2)).andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("A2"));
    }

    @Test
    void combinedFiltersAndSearchCannotBypassOwnership() throws Exception {
        expense(alice, "Lunch", "2026-10-07", "250");
        expense(alice, "Dinner", "2026-10-07", "400");
        expense(bob, "Lunch", "2026-10-07", "250");
        mvc.perform(get("/api/expenses").header("Authorization", bearer(aliceToken))
                .param("category", "food").param("search", "LUNCH")
                .param("startDate", "2026-10-07").param("endDate", "2026-10-07")
                .param("minAmount", "250").param("maxAmount", "250"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Lunch"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void anotherUsersExpenseIsUnavailableAndCannotBeChanged(String method) throws Exception {
        Expense original = expense(alice, "Private", "2026-10-07", "250");
        String path = "/api/expenses/" + original.getId();
        MockHttpServletRequestBuilder request = switch (method) {
            case "PUT" -> put(path).contentType(MediaType.APPLICATION_JSON).content(expenseBody("Changed"));
            case "DELETE" -> delete(path);
            default -> get(path);
        };
        mvc.perform(request.header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
        assertThat(expenses.findById(original.getId())).isPresent()
                .get().extracting(Expense::getTitle).isEqualTo("Private");
        mvc.perform(get("/api/expenses/9223372036854775807").header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void ownerCanReadUpdateAndDeleteTheirExpense() throws Exception {
        Expense original = expense(alice, "Lunch", "2026-10-07", "250");
        String path = "/api/expenses/" + original.getId();
        mvc.perform(get(path).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Lunch"));
        mvc.perform(put(path).header("Authorization", bearer(aliceToken))
                .contentType(MediaType.APPLICATION_JSON).content(expenseBody("Updated lunch")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated lunch"));
        assertThat(expenses.findByIdAndUserId(original.getId(), alice.getId())).isPresent();
        mvc.perform(delete(path).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        assertThat(expenses.existsById(original.getId())).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"startDate,2026-10-07,2", "endDate,2026-10-07,2"})
    void singleDateBoundWorksInclusively(String parameter, String value, int count) throws Exception {
        expense(alice, "Before", "2026-10-06", "100");
        expense(alice, "Boundary", "2026-10-07", "200");
        expense(alice, "After", "2026-10-08", "300");
        expense(bob, "Foreign boundary", "2026-10-07", "200");
        mvc.perform(get("/api/expenses").header("Authorization", bearer(aliceToken)).param(parameter, value))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(count));
    }

    @Test
    void validationUsesHttp400AsWellAsBodyStatus() throws Exception {
        mvc.perform(post("/api/expenses").header("Authorization", bearer(aliceToken))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.title").exists()).andExpect(jsonPath("$.errors.amount").exists());
    }

    @ParameterizedTest
    @CsvSource({"page,-1", "size,101", "sortBy,password", "direction,sideways", "minAmount,-1",
            "startDate,not-a-date", "size,not-a-number"})
    void invalidQueryParametersUseConsistentBadRequest(String key, String value) throws Exception {
        mvc.perform(get("/api/expenses").header("Authorization", bearer(aliceToken)).param(key, value))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void malformedJsonUsesConsistentBadRequest() throws Exception {
        mvc.perform(post("/api/expenses").header("Authorization", bearer(aliceToken))
                .contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Bearer invalid.jwt.token", "Basic unsupported"})
    void missingOrInvalidAuthenticationUsesJson401(String authorization) throws Exception {
        mvc.perform(get("/api/expenses").header("Authorization", authorization))
                .andExpect(status().isUnauthorized()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401)).andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void expiredAndIncorrectlySignedTokensAreRejected() throws Exception {
        String expired = new JwtService(signingSecret, -1000).generateToken(alice);
        String wrongKey = new JwtService("different-test-signing-secret-long-enough-for-hmac-0123456789", 86400000)
                .generateToken(alice);
        for (String token : new String[] {expired, wrongKey}) {
            mvc.perform(get("/api/expenses").header("Authorization", bearer(token)))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        }
    }

    @Test
    void tokenForDeletedUserIsRejectedWithoutServerError() throws Exception {
        users.delete(bob);
        mvc.perform(get("/api/expenses").header("Authorization", bearer(bobToken)))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void registrationLoginAndDuplicateChecksWorkWithHashedPasswords() throws Exception {
        String registration = """
                {"name":"  Charlie  ","email":"CHARLIE@example.com","password":"Password123!"}
                """;
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Charlie"))
                .andExpect(jsonPath("$.email").value("charlie@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
        User charlie = users.findByEmailIgnoreCase("charlie@example.com").orElseThrow();
        assertThat(charlie.getPassword()).isNotEqualTo("Password123!");
        assertThat(passwords.matches("Password123!", charlie.getPassword())).isTrue();
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isConflict());
        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"charlie@example.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/expenses").header("Authorization", bearer(json.readTree(login).get("token").asText())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"charlie@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void categoryDuplicatesAndInUseDeletionRemainProtected() throws Exception {
        mvc.perform(post("/api/categories").header("Authorization", bearer(aliceToken))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" food \"}"))
                .andExpect(status().isConflict());
        expense(alice, "Lunch", "2026-10-07", "250");
        mvc.perform(delete("/api/categories/" + food.getId()).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isConflict());
        assertThat(categories.existsById(food.getId())).isTrue();
    }

    private Expense expense(User owner, String title, String date, String amount) {
        return expenses.save(Expense.builder().user(owner).category(food).title(title)
                .amount(new BigDecimal(amount)).expenseDate(LocalDate.parse(date)).build());
    }

    private String expenseBody(String title) {
        return json.writeValueAsString(Map.of("title", title, "amount", 250,
                "categoryId", food.getId(), "expenseDate", "2026-10-07"));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
