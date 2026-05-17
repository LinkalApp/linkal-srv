package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Influencer;
import es.miw.tfm.linkal.domain.persistence.InfluencerPersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InfluencerServiceTest {
    @Mock
    private InfluencerPersistence influencerPersistence;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private InfluencerService influencerService;

    // -------------------------------------------------------------------------
    //  create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldEncodePassword() {
        Influencer influencer = buildInfluencer("raw123");

        when(passwordEncoder.encode("raw123")).thenReturn("hashed123");
        when(influencerPersistence.create(any())).thenReturn(influencer);

        influencerService.create(influencer);

        verify(passwordEncoder).encode("raw123");
        assertEquals("hashed123", influencer.getPassword());
    }

    @Test
    void create_shouldSetVerifiedToFalse() {
        Influencer influencer = buildInfluencer("raw123");

        when(passwordEncoder.encode(any())).thenReturn("hashed123");
        when(influencerPersistence.create(any())).thenReturn(influencer);

        influencerService.create(influencer);

        assertFalse(influencer.getVerified());
    }

    @Test
    void create_shouldDelegateToPersistence() {
        Influencer influencer = buildInfluencer("raw123");
        Influencer saved = buildInfluencer("hashed123");
        saved.setId(UUID.randomUUID());

        when(passwordEncoder.encode(any())).thenReturn("hashed123");
        when(influencerPersistence.create(any())).thenReturn(saved);

        Influencer result = influencerService.create(influencer);

        assertNotNull(result.getId());
        verify(influencerPersistence).create(influencer);
    }

    // -------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    private Influencer buildInfluencer(String password) {
        Influencer influencer = new Influencer();
        influencer.setName("Test Influencer");
        influencer.setEmail("influencer@test.com");
        influencer.setPassword(password);
        influencer.setVerified(false);
        return influencer;
    }
}
