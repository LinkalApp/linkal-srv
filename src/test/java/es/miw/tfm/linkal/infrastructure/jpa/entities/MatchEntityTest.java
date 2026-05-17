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
    void toMatch_shouldMapStatus() {
        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.COMPLETED)
                .build();

        Match match = entity.toMatch();

        assertEquals(MatchStatus.COMPLETED, match.getStatus());
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

        Match match = entity.toMatch();

        assertEquals(campaignId, match.getCampaignId());
    }

    @Test
    void toMatch_shouldLeaveCampaignIdNull_whenCampaignIsNull() {
        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .build();

        Match match = entity.toMatch();

        assertNull(match.getCampaignId());
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

        Match match = entity.toMatch();

        assertEquals(influencerId, match.getInfluencerId());
    }

    @Test
    void toMatch_shouldLeaveInfluencerIdNull_whenInfluencerIsNull() {
        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .build();

        Match match = entity.toMatch();

        assertNull(match.getInfluencerId());
    }

    @Test
    void toMatch_shouldReturnNewInstanceEachTime() {
        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .build();

        Match m1 = entity.toMatch();
        Match m2 = entity.toMatch();

        assertNotSame(m1, m2);
    }
}
