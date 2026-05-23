package es.miw.tfm.linkal.infrastructure.jpa.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordResetTokenEntityTest {

    // -------------------------------------------------------------------------
    //  Builder
    // -------------------------------------------------------------------------

    @Test
    void builder_shouldSetEmail() {
        PasswordResetTokenEntity token = PasswordResetTokenEntity.builder()
                .email("user@test.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        assertEquals("user@test.com", token.getEmail());
    }

    @Test
    void builder_shouldSetCode() {
        PasswordResetTokenEntity token = PasswordResetTokenEntity.builder()
                .email("user@test.com")
                .code("654321")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        assertEquals("654321", token.getCode());
    }

    @Test
    void builder_shouldSetExpiresAt() {
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        PasswordResetTokenEntity token = PasswordResetTokenEntity.builder()
                .email("user@test.com")
                .code("123456")
                .expiresAt(expiry)
                .build();

        assertEquals(expiry, token.getExpiresAt());
    }

    @Test
    void builder_shouldDefaultUsedToFalse() {
        PasswordResetTokenEntity token = PasswordResetTokenEntity.builder()
                .email("user@test.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        assertFalse(token.isUsed());
    }

    @Test
    void builder_shouldAllowSettingUsedToTrue() {
        PasswordResetTokenEntity token = PasswordResetTokenEntity.builder()
                .email("user@test.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(true)
                .build();

        assertTrue(token.isUsed());
    }

    // ------------------------------------------------------------------------
    //  Setters
    // ------------------------------------------------------------------------

    @Test
    void setUsed_shouldUpdateUsedField() {
        PasswordResetTokenEntity token = PasswordResetTokenEntity.builder()
                .email("user@test.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        assertFalse(token.isUsed());
        token.setUsed(true);
        assertTrue(token.isUsed());
    }

    // ------------------------------------------------------------------------
    //  Constructor sin args
    // -------------------------------------------------------------------------

    @Test
    void noArgsConstructor_shouldCreateInstanceWithNullFields() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();

        assertNull(token.getId());
        assertNull(token.getEmail());
        assertNull(token.getCode());
        assertNull(token.getExpiresAt());
    }

    // ------------------------------------------------------------------------
    //  Constructor all args
    // -------------------------------------------------------------------------

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        PasswordResetTokenEntity token = new PasswordResetTokenEntity(
                id, "user@test.com", "123456", expiry, false);

        assertEquals(id, token.getId());
        assertEquals("user@test.com", token.getEmail());
        assertEquals("123456", token.getCode());
        assertEquals(expiry, token.getExpiresAt());
        assertFalse(token.isUsed());
    }
}

