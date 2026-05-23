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

import static org.mockito.Mockito.*;
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

    // --------------------------------------------------------------------------
    //  POST /api/auth/forgot-password
    // ---------------------------------------------------------------------------

    @Test
    @WithMockUser
    void forgotPassword_shouldReturn204OnSuccess() throws Exception {
        doNothing().when(authService).sendResetCode("user@test.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "user@test.com" }
                                """))
                .andExpect(status().isNoContent());

        verify(authService).sendResetCode("user@test.com");
    }

    @Test
    @WithMockUser
    void forgotPassword_shouldReturn400WhenEmailIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void forgotPassword_shouldReturn400WhenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "not-an-email" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void forgotPassword_shouldReturn404WhenUserNotFound() throws Exception {
        doThrow(new NotFoundException("User not found: unknown@test.com"))
                .when(authService).sendResetCode("unknown@test.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "unknown@test.com" }
                                """))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    //  POST /api/auth/reset-password
    //---------------------------------------------------------------------------

    @Test
    @WithMockUser
    void resetPassword_shouldReturn204OnSuccess() throws Exception {
        doNothing().when(authService).resetPassword("user@test.com", "123456", "newPass1");

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@test.com",
                                  "code": "123456",
                                  "newPassword": "newPass1"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(authService).resetPassword("user@test.com", "123456", "newPass1");
    }

    @Test
    @WithMockUser
    void resetPassword_shouldReturn400WhenEmailIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "",
                                  "code": "123456",
                                  "newPassword": "newPass1"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void resetPassword_shouldReturn400WhenCodeIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@test.com",
                                  "code": "",
                                  "newPassword": "newPass1"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void resetPassword_shouldReturn400WhenNewPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@test.com",
                                  "code": "123456",
                                  "newPassword": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void resetPassword_shouldReturn400WhenCodeIsWrong() throws Exception {
        doThrow(new BadRequestException("Código incorrecto"))
                .when(authService).resetPassword("user@test.com", "000000", "newPass1");

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@test.com",
                                  "code": "000000",
                                  "newPassword": "newPass1"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void resetPassword_shouldReturn400WhenTokenExpired() throws Exception {
        doThrow(new BadRequestException("El código ha expirado"))
                .when(authService).resetPassword("user@test.com", "123456", "newPass1");

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@test.com",
                                  "code": "123456",
                                  "newPassword": "newPass1"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}