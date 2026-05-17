package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.model.Influencer;
import es.miw.tfm.linkal.infrastructure.jpa.entities.InfluencerEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.InfluencerRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InfluencerPersistenceJpaTest {

    @Mock
    private InfluencerRepository influencerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InfluencerPersistenceJpa influencerPersistenceJpa;

    // -------------------------------------------------------------------------
    //  create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldSaveAndReturnInfluencer() {
        Influencer influencer = buildInfluencer();
        InfluencerEntity savedEntity = buildInfluencerEntity(influencer);

        when(userRepository.existsByEmail("irene@test.com")).thenReturn(false);
        when(influencerRepository.save(any(InfluencerEntity.class))).thenReturn(savedEntity);

        Influencer result = influencerPersistenceJpa.create(influencer);

        assertNotNull(result);
        assertEquals("irene@test.com", result.getEmail());
        verify(influencerRepository).save(any(InfluencerEntity.class));
    }

    @Test
    void create_shouldThrowConflictExceptionWhenEmailAlreadyExists() {
        Influencer influencer = buildInfluencer();

        when(userRepository.existsByEmail("irene@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> influencerPersistenceJpa.create(influencer));
        verify(influencerRepository, never()).save(any());
    }

    @Test
    void create_shouldCheckEmailBeforeSaving() {
        Influencer influencer = buildInfluencer();
        InfluencerEntity savedEntity = buildInfluencerEntity(influencer);

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(influencerRepository.save(any())).thenReturn(savedEntity);

        influencerPersistenceJpa.create(influencer);

        // Verificamos el orden: primero existsByEmail, luego save
        var inOrder = inOrder(userRepository, influencerRepository);
        inOrder.verify(userRepository).existsByEmail("irene@test.com");
        inOrder.verify(influencerRepository).save(any());
    }

    // -------------------------------------------------------------------------
    //  Helpers
    // -------------------------------------------------------------------------

    private Influencer buildInfluencer() {
        Influencer influencer = new Influencer();
        influencer.setName("Irene");
        influencer.setEmail("irene@test.com");
        influencer.setPassword("hashedPass");
        influencer.setArtisticName("ArtistIrene");
        influencer.setInstagram("@irene_ig");
        influencer.setTiktok("@irene_tt");
        influencer.setYoutube("@irene_yt");
        influencer.setVerified(false);
        return influencer;
    }

    private InfluencerEntity buildInfluencerEntity(Influencer influencer) {
        InfluencerEntity entity = new InfluencerEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(influencer.getName());
        entity.setEmail(influencer.getEmail());
        entity.setPassword(influencer.getPassword());
        entity.setArtisticName(influencer.getArtisticName());
        entity.setInstagram(influencer.getInstagram());
        entity.setTiktok(influencer.getTiktok());
        entity.setYoutube(influencer.getYoutube());
        entity.setVerified(false);
        entity.setInterests(new ArrayList<>());
        return entity;
    }
}
