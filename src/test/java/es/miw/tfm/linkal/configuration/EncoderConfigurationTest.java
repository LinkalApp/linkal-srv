package es.miw.tfm.linkal.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class EncoderConfigurationTest {

    @Test
    void passwordEncoder_shouldReturnBCryptPasswordEncoder() {
        EncoderConfiguration config = new EncoderConfiguration();

        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    void passwordEncoder_shouldEncodeAndMatchPassword() {
        EncoderConfiguration config = new EncoderConfiguration();
        PasswordEncoder encoder = config.passwordEncoder();

        String raw = "myPassword123";
        String encoded = encoder.encode(raw);

        assertTrue(encoder.matches(raw, encoded));
        assertNotEquals(raw, encoded);
    }
}

