package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
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
import java.util.List;
import java.util.Optional;
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

    // ------------------------------------------------------------------------
    //  readMe
    // -------------------------------------------------------------------------

    @Test
    void readMe_shouldReturnInfluencerWhenEmailFound() {
        InfluencerEntity entity = buildInfluencerEntity(buildInfluencer());

        when(influencerRepository.findByEmail("irene@test.com")).thenReturn(Optional.of(entity));

        Influencer result = influencerPersistenceJpa.readMe("irene@test.com");

        assertEquals("irene@test.com", result.getEmail());
        assertEquals("ArtistIrene", result.getArtisticName());
    }

    @Test
    void readMe_shouldThrowNotFoundExceptionWhenEmailNotFound() {
        when(influencerRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> influencerPersistenceJpa.readMe("unknown@test.com"));
    }

    // ------------------------------------------------------------------------
    //  updateMe
    // -------------------------------------------------------------------------

    @Test
    void updateMe_shouldUpdateFieldsAndSave() {
        InfluencerEntity existing = buildInfluencerEntity(buildInfluencer());

        Influencer patch = new Influencer();
        patch.setArtisticName("NewArtist");
        patch.setInstagram("@new_ig");

        when(influencerRepository.findByEmail("irene@test.com")).thenReturn(Optional.of(existing));
        when(influencerRepository.save(existing)).thenReturn(existing);

        Influencer result = influencerPersistenceJpa.updateMe("irene@test.com", patch);

        assertEquals("NewArtist", existing.getArtisticName());
        assertEquals("@new_ig", existing.getInstagram());
        verify(influencerRepository).save(existing);
    }

    @Test
    void updateMe_shouldThrowNotFoundExceptionWhenEmailNotFound() {
        when(influencerRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> influencerPersistenceJpa.updateMe("unknown@test.com", new Influencer()));
        verify(influencerRepository, never()).save(any());
    }

    @Test
    void updateMe_shouldNotOverwriteFieldsWhenPatchValueIsNull() {
        InfluencerEntity existing = buildInfluencerEntity(buildInfluencer());

        Influencer patch = new Influencer(); // todos null

        when(influencerRepository.findByEmail("irene@test.com")).thenReturn(Optional.of(existing));
        when(influencerRepository.save(existing)).thenReturn(existing);

        influencerPersistenceJpa.updateMe("irene@test.com", patch);

        assertEquals("ArtistIrene", existing.getArtisticName());
        assertEquals("@irene_ig",   existing.getInstagram());
        assertEquals("@irene_tt",   existing.getTiktok());
        assertEquals("@irene_yt",   existing.getYoutube());
    }

    @Test
    void updateMe_shouldClearSocialFieldWhenEmptyStringIsSent() {
        InfluencerEntity existing = buildInfluencerEntity(buildInfluencer());
        // tiktok tenía valor; el usuario lo borra enviando ""
        existing.setTiktok("@irene_tt");

        Influencer patch = new Influencer();
        patch.setTiktok(""); // vaciar

        when(influencerRepository.findByEmail("irene@test.com")).thenReturn(Optional.of(existing));
        when(influencerRepository.save(existing)).thenReturn(existing);

        influencerPersistenceJpa.updateMe("irene@test.com", patch);

        assertEquals("", existing.getTiktok());
        verify(influencerRepository).save(existing);
    }

    @Test
    void updateMe_shouldThrowConflictExceptionWhenNoSocialNetworkRemains() {
        InfluencerEntity existing = buildInfluencerEntity(buildInfluencer());
        // El usuario borra las tres redes enviando cadenas vacías
        Influencer patch = new Influencer();
        patch.setInstagram("");
        patch.setTiktok("");
        patch.setYoutube("");

        when(influencerRepository.findByEmail("irene@test.com")).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class,
                () -> influencerPersistenceJpa.updateMe("irene@test.com", patch));
        verify(influencerRepository, never()).save(any());
    }

    @Test
    void updateMe_shouldUpdateInterestsList() {
        InfluencerEntity existing = buildInfluencerEntity(buildInfluencer());
        existing.setInterests(new ArrayList<>(List.of("Moda")));

        Influencer patch = new Influencer();
        patch.setInterests(List.of("Viajes", "Fitness"));

        when(influencerRepository.findByEmail("irene@test.com")).thenReturn(Optional.of(existing));
        when(influencerRepository.save(existing)).thenReturn(existing);

        influencerPersistenceJpa.updateMe("irene@test.com", patch);

        assertEquals(List.of("Viajes", "Fitness"), existing.getInterests());
    }

    @Test
    void updateMe_shouldUpdateAllSocialNetworksSimultaneously() {
        InfluencerEntity existing = buildInfluencerEntity(buildInfluencer());

        Influencer patch = new Influencer();
        patch.setInstagram("@new_ig");
        patch.setTiktok("@new_tt");
        patch.setYoutube("@new_yt");

        when(influencerRepository.findByEmail("irene@test.com")).thenReturn(Optional.of(existing));
        when(influencerRepository.save(existing)).thenReturn(existing);

        influencerPersistenceJpa.updateMe("irene@test.com", patch);

        assertEquals("@new_ig", existing.getInstagram());
        assertEquals("@new_tt", existing.getTiktok());
        assertEquals("@new_yt", existing.getYoutube());
    }

    // --------------------------------------------------------------------------
    //  deleteMe
    // --------------------------------------------------------------------------

    @Test
    void deleteMe_shouldDeleteEntityWhenEmailFound() {
        InfluencerEntity entity = buildInfluencerEntity(buildInfluencer());

        when(influencerRepository.findByEmail("irene@test.com")).thenReturn(Optional.of(entity));

        influencerPersistenceJpa.deleteMe("irene@test.com");

        verify(influencerRepository).delete(entity);
    }

    @Test
    void deleteMe_shouldThrowNotFoundExceptionWhenEmailNotFound() {
        when(influencerRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> influencerPersistenceJpa.deleteMe("unknown@test.com"));
        verify(influencerRepository, never()).delete(any());
    }

    @Test
    void deleteMe_shouldNotDeleteAnyOtherEntity() {
        InfluencerEntity entity = buildInfluencerEntity(buildInfluencer());

        when(influencerRepository.findByEmail("irene@test.com")).thenReturn(Optional.of(entity));

        influencerPersistenceJpa.deleteMe("irene@test.com");

        verify(influencerRepository, times(1)).delete(entity);
        verify(influencerRepository, never()).deleteAll();
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
