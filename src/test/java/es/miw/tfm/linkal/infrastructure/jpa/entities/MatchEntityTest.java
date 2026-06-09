package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.Match;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MatchEntityTest {

    // -------------------------------------------------------------------------
    //  toMatch()
    // -------------------------------------------------------------------------

    @Test
    void toMatch_shouldMapIdAndCreatedAt() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        MatchEntity entity = MatchEntity.builder()
                .id(id)
                .createdAt(now)
                .status(MatchStatus.PENDING)
                .build();

        Match match = entity.toMatch();

        assertEquals(id, match.getId());
        assertEquals(now, match.getCreatedAt());
    }

    @Test
    void toMatch_shouldMapStatusPending() {
        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .build();

        assertEquals(MatchStatus.PENDING, entity.toMatch().getStatus());
    }

    @Test
    void toMatch_shouldMapStatusCompleted() {
        LocalDateTime matchedAt = LocalDateTime.now();

        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.COMPLETED)
                .matchedAt(matchedAt)
                .build();

        Match match = entity.toMatch();

        assertEquals(MatchStatus.COMPLETED, match.getStatus());
        assertEquals(matchedAt, match.getMatchedAt());
    }

    @Test
    void toMatch_shouldLeaveMatchedAtNull_whenNotCompleted() {
        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .build();

        assertNull(entity.toMatch().getMatchedAt());
    }

    @Test
    void toMatch_shouldSetCampaignId_whenCampaignIsNotNull() {
        UUID campaignId = UUID.randomUUID();
        CampaignEntity campaign = new CampaignEntity();
        campaign.setId(campaignId);

        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .campaign(campaign)
                .build();

        assertEquals(campaignId, entity.toMatch().getCampaignId());
    }

    @Test
    void toMatch_shouldLeaveCampaignIdNull_whenCampaignIsNull() {
        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .build();

        assertNull(entity.toMatch().getCampaignId());
    }

    @Test
    void toMatch_shouldSetInfluencerId_whenInfluencerIsNotNull() {
        UUID influencerId = UUID.randomUUID();
        InfluencerEntity influencer = new InfluencerEntity();
        influencer.setId(influencerId);

        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .influencer(influencer)
                .build();

        assertEquals(influencerId, entity.toMatch().getInfluencerId());
    }

    @Test
    void toMatch_shouldLeaveInfluencerIdNull_whenInfluencerIsNull() {
        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .build();

        assertNull(entity.toMatch().getInfluencerId());
    }

    @Test
    void toMatch_shouldMapBusinessId_whenSet() {
        UUID businessId = UUID.randomUUID();

        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .businessId(businessId)
                .build();

        assertEquals(businessId, entity.toMatch().getBusinessId());
    }

    @Test
    void toMatch_shouldLeaveBusinessIdNull_whenNotSet() {
        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .build();

        assertNull(entity.toMatch().getBusinessId());
    }

    // -------------------------------------------------------------------------
    //  toMatch() — nueva instancia en cada llamada
    // -------------------------------------------------------------------------

    @Test
    void toMatch_shouldReturnNewInstanceEachTime() {
        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .build();

        assertNotSame(entity.toMatch(), entity.toMatch());
    }

    // -------------------------------------------------------------------------
    // Builder por defecto
    // -------------------------------------------------------------------------

    @Test
    void builder_shouldDefaultStatusToPending() {
        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        assertEquals(MatchStatus.PENDING, entity.getStatus());
    }
}
