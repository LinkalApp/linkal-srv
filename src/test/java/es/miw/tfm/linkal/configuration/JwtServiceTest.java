package es.miw.tfm.linkal.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private JwtService jwtService;

    private static final String SECRET = "linkal-test-secret-key-must-be-at-least-32-chars!!";
    private static final long EXPIRATION_MS = 86400000L; // 24h

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", EXPIRATION_MS);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtService.generateToken("test@test.com", "BUSINESS");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_shouldContainThreeParts() {
        String token = jwtService.generateToken("test@test.com", "BUSINESS");
        // JWT tiene 3 partes separadas por "."
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String email = "irene@linkal.com";
        String token = jwtService.generateToken(email, "BUSINESS");
        assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void extractRole_shouldReturnCorrectRole() {
        String role = "INFLUENCER";
        String token = jwtService.generateToken("test@test.com", role);
        assertEquals(role, jwtService.extractRole(token));
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtService.generateToken("test@test.com", "BUSINESS");
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalseForRandomString() {
        assertFalse(jwtService.isTokenValid("this.is.not.a.valid.token"));
    }

    @Test
    void isTokenValid_shouldReturnFalseForEmptyString() {
        assertFalse(jwtService.isTokenValid(""));
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        // Expiration negativa genera token ya caducado
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String token = jwtService.generateToken("test@test.com", "BUSINESS");
        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void generateToken_differentEmailsShouldProduceDifferentTokens() {
        String token1 = jwtService.generateToken("user1@test.com", "BUSINESS");
        String token2 = jwtService.generateToken("user2@test.com", "BUSINESS");
        assertNotEquals(token1, token2);
    }
}
