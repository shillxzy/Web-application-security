package chnu.edu.booksecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
public class AccessTests {

    private static final String NEW_BOOK = """
            {"title": "Test Book", "author": "Tester", "publishedYear": 2026}
            """;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    // ---------- anonymous and wrong credentials ----------

    @Test
    @WithAnonymousUser
    void whenAnonymousGetsBooksThenUnauthorized() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/books"));

        var status = result.andReturn().getResponse().getStatus();
        var message = result.andReturn().getResponse().getErrorMessage();
        assertEquals(401, status);
        assertEquals("Unauthorized", message);
    }

    @Test
    void whenWrongPasswordThenUnauthorized() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/books")
                .with(httpBasic("user", "wrongpass")));

        assertEquals(401, result.andReturn().getResponse().getStatus());
    }

    @Test
    void whenRealUserCredentialsThenOk() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/books")
                .with(httpBasic("user", "user")));

        assertEquals(200, result.andReturn().getResponse().getStatus());
    }

    // ---------- books: read ----------

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserGetsAllBooksThenOk() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/books"));

        var status = result.andReturn().getResponse().getStatus();
        var content = result.andReturn().getResponse().getContentAsString();
        assertEquals(200, status);
        assertTrue(content.contains("Clean Code"));
    }

    @Test
    @WithMockUser(username = "superadmin", roles = {"SUPERADMIN"})
    void whenSuperadminGetsOneBookThenOk() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/books/2"));

        var status = result.andReturn().getResponse().getStatus();
        var content = result.andReturn().getResponse().getContentAsString();
        assertEquals(200, status);
        assertTrue(content.contains("Clean Code"));
    }

    // ---------- books: create / update ----------

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserCreatesBookThenForbidden() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_BOOK));

        var status = result.andReturn().getResponse().getStatus();
        var message = result.andReturn().getResponse().getErrorMessage();
        assertEquals(403, status);
        assertEquals("Forbidden", message);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminCreatesBookThenOkAndAudited() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEW_BOOK));

        var status = result.andReturn().getResponse().getStatus();
        var content = result.andReturn().getResponse().getContentAsString();
        assertEquals(200, status);
        assertTrue(content.contains("\"createdBy\":\"admin\""));
    }

    @Test
    @WithMockUser(username = "superadmin", roles = {"SUPERADMIN"})
    void whenSuperadminUpdatesBookThenOk() throws Exception {
        ResultActions result = mockMvc.perform(put("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id": 1, "title": "Kobzar (updated)", "author": "Taras Shevchenko", "publishedYear": 1840}
                        """));

        var status = result.andReturn().getResponse().getStatus();
        var content = result.andReturn().getResponse().getContentAsString();
        assertEquals(200, status);
        assertTrue(content.contains("\"lastModifiedBy\":\"superadmin\""));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserUpdatesBookThenForbidden() throws Exception {
        ResultActions result = mockMvc.perform(put("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id": 1, "title": "Hacked", "author": "Nobody", "publishedYear": 1}
                        """));

        assertEquals(403, result.andReturn().getResponse().getStatus());
    }

    // ---------- books: delete ----------

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminDeletesBookThenForbidden() throws Exception {
        ResultActions result = mockMvc.perform(delete("/api/v1/books/2"));

        var status = result.andReturn().getResponse().getStatus();
        var message = result.andReturn().getResponse().getErrorMessage();
        assertEquals(403, status);
        assertEquals("Forbidden", message);
    }

    @Test
    @WithMockUser(username = "superadmin", roles = {"SUPERADMIN"})
    void whenSuperadminDeletesBookThenOk() throws Exception {
        ResultActions result = mockMvc.perform(delete("/api/v1/books/3"));

        assertEquals(200, result.andReturn().getResponse().getStatus());
    }

    // ---------- hello routes ----------

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminGetsHelloAdminThenOk() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/hello/admin"));

        var status = result.andReturn().getResponse().getStatus();
        var content = result.andReturn().getResponse().getContentAsString();
        assertEquals(200, status);
        assertEquals("Hello, admin!", content);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserGetsHelloAdminThenForbidden() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/hello/admin"));

        assertEquals(403, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminGetsHelloUserThenForbidden() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/hello/user"));

        var status = result.andReturn().getResponse().getStatus();
        var message = result.andReturn().getResponse().getErrorMessage();
        assertEquals(403, status);
        assertEquals("Forbidden", message);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserGetsHelloUserThenOk() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/hello/user"));

        var status = result.andReturn().getResponse().getStatus();
        var content = result.andReturn().getResponse().getContentAsString();
        assertEquals(200, status);
        assertEquals("Hello, user!", content);
    }

    @Test
    @WithMockUser(username = "superadmin", roles = {"SUPERADMIN"})
    void whenSuperadminGetsHelloManThenOk() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/hello/man"));

        var status = result.andReturn().getResponse().getStatus();
        var content = result.andReturn().getResponse().getContentAsString();
        assertEquals(200, status);
        assertEquals("Hello, man!", content);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserGetsHelloManThenForbidden() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/hello/man"));

        assertEquals(403, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserGetsRouteWithoutAnnotationThenOk() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/hello/unknown"));

        var status = result.andReturn().getResponse().getStatus();
        var content = result.andReturn().getResponse().getContentAsString();
        assertEquals(200, status);
        assertEquals("Hello, unknown!", content);
    }
}
