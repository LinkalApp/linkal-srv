package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Match;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import es.miw.tfm.linkal.infrastructure.jpa.entities.CampaignEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.InfluencerEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.MatchEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.CampaignRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.InfluencerRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchPersistenceJpaTest {
    @Mock private MatchRepository matchRepository;
    @Mock private CampaignRepository campaignRepository;
    @Mock private InfluencerRepository influencerRepository;

    @InjectMocks
    private MatchPersistenceJpa matchPersistenceJpa;

    // ---------------------------------------------------------------------------
    //  createByInfluencer
    // ---------------------------------------------------------------------------

    @Test
    void createByInfluencer_shouldThrowNotFound_whenCampaignDoesNotExist() {
        when(campaignRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> matchPersistenceJpa.createByInfluencer(UUID.randomUUID(), "influencer@test.com"));
        verify(matchRepository, never()).save(any());
    }

    @Test
    void createByInfluencer_shouldThrowNotFound_whenInfluencerDoesNotExist() {
        CampaignEntity campaign = buildCampaignEntity();
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(influencerRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> matchPersistenceJpa.createByInfluencer(campaign.getId(), "unknown@test.com"));
        verify(matchRepository, never()).save(any());
    }

    @Test
    void createByInfluencer_shouldThrowConflict_whenInfluencerAlreadyExpressedInterest() {
        CampaignEntity campaign = buildCampaignEntity();
        InfluencerEntity influencer = buildInfluencerEntity();
        MatchEntity existing = buildPendingMatchByInfluencer(campaign, influencer);

        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(influencerRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencer));
        when(matchRepository.findByCampaign_IdAndInfluencer_Id(campaign.getId(), influencer.getId()))
                .thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class,
                () -> matchPersistenceJpa.createByInfluencer(campaign.getId(), "influencer@test.com"));
        verify(matchRepository, never()).save(any());
    }

    @Test
    void createByInfluencer_shouldThrowConflict_whenMatchIsAlreadyCompleted() {
        CampaignEntity campaign = buildCampaignEntity();
        InfluencerEntity influencer = buildInfluencerEntity();
        MatchEntity existing = buildCompletedMatch(campaign, influencer);

        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(influencerRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencer));
        when(matchRepository.findByCampaign_IdAndInfluencer_Id(campaign.getId(), influencer.getId()))
                .thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class,
                () -> matchPersistenceJpa.createByInfluencer(campaign.getId(), "influencer@test.com"));
        verify(matchRepository, never()).save(any());
    }

    @Test
    void createByInfluencer_shouldCompleteMatch_whenBusinessInitiated() {
        CampaignEntity campaign = buildCampaignEntity();
        InfluencerEntity influencer = buildInfluencerEntity();
        MatchEntity existingByBusiness = buildPendingMatchByBusiness(campaign);

        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(influencerRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencer));
        when(matchRepository.findByCampaign_IdAndInfluencer_Id(campaign.getId(), influencer.getId()))
                .thenReturn(Optional.empty());
        when(matchRepository.findByCampaign_IdAndInfluencerIsNull(campaign.getId()))
                .thenReturn(Optional.of(existingByBusiness));
        when(matchRepository.save(any())).thenReturn(existingByBusiness);

        matchPersistenceJpa.createByInfluencer(campaign.getId(), "influencer@test.com");

        ArgumentCaptor<MatchEntity> captor = ArgumentCaptor.forClass(MatchEntity.class);
        verify(matchRepository).save(captor.capture());
        assertEquals(MatchStatus.COMPLETED, captor.getValue().getStatus());
        assertEquals(influencer, captor.getValue().getInfluencer());
        assertNotNull(captor.getValue().getMatchedAt());
    }

    @Test
    void createByInfluencer_shouldNotCreateNewRecord_whenBusinessInitiated() {
        CampaignEntity campaign = buildCampaignEntity();
        InfluencerEntity influencer = buildInfluencerEntity();
        MatchEntity existing = buildPendingMatchByBusiness(campaign);

        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(influencerRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencer));
        when(matchRepository.findByCampaign_IdAndInfluencer_Id(any(), any())).thenReturn(Optional.empty());
        when(matchRepository.findByCampaign_IdAndInfluencerIsNull(any())).thenReturn(Optional.of(existing));
        when(matchRepository.save(any())).thenReturn(existing);

        matchPersistenceJpa.createByInfluencer(campaign.getId(), "influencer@test.com");

        verify(matchRepository, times(1)).save(any());
    }

    @Test
    void createByInfluencer_shouldCreatePendingMatch_whenNoExistingMatch() {
        CampaignEntity campaign = buildCampaignEntity();
        InfluencerEntity influencer = buildInfluencerEntity();
        MatchEntity saved = buildPendingMatchByInfluencer(campaign, influencer);

        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(influencerRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencer));
        when(matchRepository.findByCampaign_IdAndInfluencer_Id(any(), any())).thenReturn(Optional.empty());
        when(matchRepository.findByCampaign_IdAndInfluencerIsNull(any())).thenReturn(Optional.empty());
        when(matchRepository.save(any())).thenReturn(saved);

        Match result = matchPersistenceJpa.createByInfluencer(campaign.getId(), "influencer@test.com");

        ArgumentCaptor<MatchEntity> captor = ArgumentCaptor.forClass(MatchEntity.class);
        verify(matchRepository).save(captor.capture());
        assertEquals(MatchStatus.PENDING, captor.getValue().getStatus());
        assertNull(captor.getValue().getBusinessId());
        assertNotNull(result);
    }

    @Test
    void createByInfluencer_shouldSetInfluencerOnNewMatch() {
        CampaignEntity campaign = buildCampaignEntity();
        InfluencerEntity influencer = buildInfluencerEntity();

        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(influencerRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencer));
        when(matchRepository.findByCampaign_IdAndInfluencer_Id(any(), any())).thenReturn(Optional.empty());
        when(matchRepository.findByCampaign_IdAndInfluencerIsNull(any())).thenReturn(Optional.empty());
        when(matchRepository.save(any())).thenReturn(buildPendingMatchByInfluencer(campaign, influencer));

        matchPersistenceJpa.createByInfluencer(campaign.getId(), "influencer@test.com");

        ArgumentCaptor<MatchEntity> captor = ArgumentCaptor.forClass(MatchEntity.class);
        verify(matchRepository).save(captor.capture());
        assertEquals(influencer, captor.getValue().getInfluencer());
        assertEquals(campaign, captor.getValue().getCampaign());
    }

    // -------------------------------------------------------------------------
    //  findByInfluencer
    // -------------------------------------------------------------------------

    @Test
    void findByInfluencer_shouldThrowNotFound_whenInfluencerDoesNotExist() {
        when(influencerRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> matchPersistenceJpa.findByInfluencer(UUID.randomUUID(), "unknown@test.com"));
    }

    @Test
    void findByInfluencer_shouldReturnMatch_whenExists() {
        CampaignEntity campaign = buildCampaignEntity();
        InfluencerEntity influencer = buildInfluencerEntity();
        MatchEntity matchEntity = buildPendingMatchByInfluencer(campaign, influencer);

        when(influencerRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencer));
        when(matchRepository.findByCampaign_IdAndInfluencer_Id(campaign.getId(), influencer.getId()))
                .thenReturn(Optional.of(matchEntity));

        Optional<Match> result = matchPersistenceJpa.findByInfluencer(campaign.getId(), "influencer@test.com");

        assertTrue(result.isPresent());
        assertEquals(MatchStatus.PENDING, result.get().getStatus());
    }

    @Test
    void findByInfluencer_shouldReturnEmpty_whenNotExists() {
        InfluencerEntity influencer = buildInfluencerEntity();

        when(influencerRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencer));
        when(matchRepository.findByCampaign_IdAndInfluencer_Id(any(), any())).thenReturn(Optional.empty());

        Optional<Match> result = matchPersistenceJpa.findByInfluencer(UUID.randomUUID(), "influencer@test.com");

        assertFalse(result.isPresent());
    }

    // ------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    private CampaignEntity buildCampaignEntity() {
        CampaignEntity c = new CampaignEntity();
        c.setId(UUID.randomUUID());
        return c;
    }

    private InfluencerEntity buildInfluencerEntity() {
        InfluencerEntity i = new InfluencerEntity();
        i.setId(UUID.randomUUID());
        return i;
    }

    private MatchEntity buildPendingMatchByInfluencer(CampaignEntity campaign, InfluencerEntity influencer) {
        return MatchEntity.builder()
                .id(UUID.randomUUID())
                .campaign(campaign)
                .influencer(influencer)
                .businessId(null)
                .status(MatchStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private MatchEntity buildPendingMatchByBusiness(CampaignEntity campaign) {
        return MatchEntity.builder()
                .id(UUID.randomUUID())
                .campaign(campaign)
                .influencer(null)
                .businessId(UUID.randomUUID())
                .status(MatchStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private MatchEntity buildCompletedMatch(CampaignEntity campaign, InfluencerEntity influencer) {
        return MatchEntity.builder()
                .id(UUID.randomUUID())
                .campaign(campaign)
                .influencer(influencer)
                .businessId(UUID.randomUUID())
                .status(MatchStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .matchedAt(LocalDateTime.now())
                .build();
    }
}
