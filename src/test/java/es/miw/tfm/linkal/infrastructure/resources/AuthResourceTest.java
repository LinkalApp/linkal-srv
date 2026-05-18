package es.miw.tfm.linkal.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.miw.tfm.linkal.configuration.JwtService;
import es.miw.tfm.linkal.domain.exceptions.BadRequestException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthResource.class)
class AuthResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    // --------------------------------------------------------------------------
    //  POST /api/auth/login — público (permitAll)
    // --------------------------------------------------------------------------

    @Test
    @WithMockUser
    void login_shouldReturn200WithTokenOnValidCredentials() throws Exception {
        AuthService.AuthResponse response =
                new AuthService.AuthResponse("jwt-token-abc", "BUSINESS", "user@test.com");

        when(authService.login("user@test.com", "pass123")).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@test.com",
                                  "password": "pass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-abc"))
                .andExpect(jsonPath("$.role").value("BUSINESS"))
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    @WithMockUser
    void login_shouldReturn200ForInfluencer() throws Exception {
        AuthService.AuthResponse response =
                new AuthService.AuthResponse("jwt-influencer", "INFLUENCER", "influencer@test.com");

        when(authService.login("influencer@test.com", "myPass")).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "influencer@test.com",
                                  "password": "myPass"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("INFLUENCER"));
    }

    @Test
    @WithMockUser
    void login_shouldReturn400WhenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-valid-email",
                                  "password": "pass123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void login_shouldReturn400WhenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@test.com",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void login_shouldReturn404WhenUserNotFound() throws Exception {
        when(authService.login("unknown@test.com", "pass"))
                .thenThrow(new NotFoundException("User not found: unknown@test.com"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "unknown@test.com",
                                  "password": "pass"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void login_shouldReturn400WhenCredentialsAreInvalid() throws Exception {
        when(authService.login("user@test.com", "wrongPass"))
                .thenThrow(new BadRequestException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@test.com",
                                  "password": "wrongPass"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}