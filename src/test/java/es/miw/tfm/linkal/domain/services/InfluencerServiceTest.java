package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Influencer;
import es.miw.tfm.linkal.domain.persistence.EvaluationPersistence;
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
    private EvaluationPersistence evaluationPersistence;

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

    // -----------------------------------------------------------------------
    //  readMe
    // ------------------------------------------------------------------------

    @Test
    void readMe_shouldReturnInfluencerWithNullPassword() {
        Influencer influencer = buildInfluencer("hashedPass");

        when(influencerPersistence.readMe("influencer@test.com")).thenReturn(influencer);

        Influencer result = influencerService.readMe("influencer@test.com");

        assertNull(result.getPassword());
        assertEquals("influencer@test.com", result.getEmail());
    }

    @Test
    void readMe_shouldDelegateToPersistence() {
        Influencer influencer = buildInfluencer("hashedPass");

        when(influencerPersistence.readMe("influencer@test.com")).thenReturn(influencer);

        influencerService.readMe("influencer@test.com");

        verify(influencerPersistence).readMe("influencer@test.com");
    }

    @Test
    void readMe_shouldSetAverageRating() {
        UUID id = UUID.randomUUID();
        Influencer influencer = buildInfluencer("hashedPass");
        influencer.setId(id);

        when(influencerPersistence.readMe("influencer@test.com")).thenReturn(influencer);
        when(evaluationPersistence.averageScoreByInfluencerId(id)).thenReturn(4.2);

        Influencer result = influencerService.readMe("influencer@test.com");

        assertEquals(4.2, result.getAverageRating());
        verify(evaluationPersistence).averageScoreByInfluencerId(id);
    }

    @Test
    void readMe_shouldSetAverageRatingNullWhenNoEvaluations() {
        UUID id = UUID.randomUUID();
        Influencer influencer = buildInfluencer("hashedPass");
        influencer.setId(id);

        when(influencerPersistence.readMe("influencer@test.com")).thenReturn(influencer);
        when(evaluationPersistence.averageScoreByInfluencerId(id)).thenReturn(null);

        Influencer result = influencerService.readMe("influencer@test.com");

        assertNull(result.getAverageRating());
    }

    // -------------------------------------------------------------------------
    //  updateMe
    // -------------------------------------------------------------------------

    @Test
    void updateMe_shouldDelegateToPersistence() {
        Influencer patch = new Influencer();
        patch.setInstagram("@new_ig");

        Influencer updated = buildInfluencer("hashed");
        updated.setId(UUID.randomUUID());
        updated.setInstagram("@new_ig");

        when(influencerPersistence.updateMe("influencer@test.com", patch)).thenReturn(updated);
        when(evaluationPersistence.averageScoreByInfluencerId(updated.getId())).thenReturn(null);

        influencerService.updateMe("influencer@test.com", patch);

        verify(influencerPersistence).updateMe("influencer@test.com", patch);
    }

    @Test
    void updateMe_shouldReturnUpdatedInfluencerWithNullPassword() {
        Influencer patch = new Influencer();
        patch.setArtisticName("NewArtist");

        Influencer updated = buildInfluencer("hashedPass");
        updated.setId(UUID.randomUUID());
        updated.setArtisticName("NewArtist");

        when(influencerPersistence.updateMe("influencer@test.com", patch)).thenReturn(updated);
        when(evaluationPersistence.averageScoreByInfluencerId(updated.getId())).thenReturn(null);

        Influencer result = influencerService.updateMe("influencer@test.com", patch);

        assertNull(result.getPassword());
        assertEquals("NewArtist", result.getArtisticName());
    }

    @Test
    void updateMe_shouldSetAverageRating() {
        UUID id = UUID.randomUUID();
        Influencer patch = new Influencer();

        Influencer updated = buildInfluencer("hashed");
        updated.setId(id);

        when(influencerPersistence.updateMe("influencer@test.com", patch)).thenReturn(updated);
        when(evaluationPersistence.averageScoreByInfluencerId(id)).thenReturn(3.5);

        Influencer result = influencerService.updateMe("influencer@test.com", patch);

        assertEquals(3.5, result.getAverageRating());
        verify(evaluationPersistence).averageScoreByInfluencerId(id);
    }

    @Test
    void updateMe_shouldSetAverageRatingNullWhenNoEvaluations() {
        UUID id = UUID.randomUUID();
        Influencer patch = new Influencer();

        Influencer updated = buildInfluencer("hashed");
        updated.setId(id);

        when(influencerPersistence.updateMe("influencer@test.com", patch)).thenReturn(updated);
        when(evaluationPersistence.averageScoreByInfluencerId(id)).thenReturn(null);

        Influencer result = influencerService.updateMe("influencer@test.com", patch);

        assertNull(result.getAverageRating());
    }

    @Test
    void updateMe_shouldPreserveSocialNetworkValues() {
        UUID id = UUID.randomUUID();
        Influencer patch = new Influencer();
        patch.setTiktok("@new_tiktok");

        Influencer updated = buildInfluencer("hashed");
        updated.setId(id);
        updated.setTiktok("@new_tiktok");

        when(influencerPersistence.updateMe("influencer@test.com", patch)).thenReturn(updated);
        when(evaluationPersistence.averageScoreByInfluencerId(id)).thenReturn(null);

        Influencer result = influencerService.updateMe("influencer@test.com", patch);

        assertEquals("@new_tiktok", result.getTiktok());
    }

    // ------------------------------------------------------------------------
    //  deleteMe
    // -------------------------------------------------------------------------

    @Test
    void deleteMe_shouldDelegateToPersistence() {
        doNothing().when(influencerPersistence).deleteMe("influencer@test.com");

        influencerService.deleteMe("influencer@test.com");

        verify(influencerPersistence).deleteMe("influencer@test.com");
    }

    @Test
    void deleteMe_shouldPropagateNotFoundExceptionFromPersistence() {
        doThrow(new es.miw.tfm.linkal.domain.exceptions.NotFoundException("not found"))
                .when(influencerPersistence).deleteMe("unknown@test.com");

        assertThrows(es.miw.tfm.linkal.domain.exceptions.NotFoundException.class,
                () -> influencerService.deleteMe("unknown@test.com"));
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
