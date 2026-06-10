package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Match;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import es.miw.tfm.linkal.domain.persistence.MatchPersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {
    @Mock
    private MatchPersistence matchPersistence;

    @InjectMocks
    private MatchService matchService;

    // --------------------------------------------------------------------------
    //  createByInfluencer
    // --------------------------------------------------------------------------

    @Test
    void createByInfluencer_shouldDelegateToPersistence() {
        UUID campaignId = UUID.randomUUID();
        Match match = buildPendingMatch(campaignId);

        when(matchPersistence.createByInfluencer(campaignId, "influencer@test.com")).thenReturn(match);

        matchService.createByInfluencer(campaignId, "influencer@test.com");

        verify(matchPersistence).createByInfluencer(campaignId, "influencer@test.com");
    }

    @Test
    void createByInfluencer_shouldReturnMatchFromPersistence() {
        UUID campaignId = UUID.randomUUID();
        Match match = buildPendingMatch(campaignId);

        when(matchPersistence.createByInfluencer(campaignId, "influencer@test.com")).thenReturn(match);

        Match result = matchService.createByInfluencer(campaignId, "influencer@test.com");

        assertNotNull(result);
        assertEquals(MatchStatus.PENDING, result.getStatus());
        assertEquals(campaignId, result.getCampaignId());
    }

    @Test
    void createByInfluencer_shouldReturnCompletedMatchWhenMutual() {
        UUID campaignId = UUID.randomUUID();
        Match completed = buildCompletedMatch(campaignId);

        when(matchPersistence.createByInfluencer(campaignId, "influencer@test.com")).thenReturn(completed);

        Match result = matchService.createByInfluencer(campaignId, "influencer@test.com");

        assertEquals(MatchStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getMatchedAt());
    }

    @Test
    void createByInfluencer_shouldPassExactEmailToPersistence() {
        UUID campaignId = UUID.randomUUID();
        when(matchPersistence.createByInfluencer(any(), any())).thenReturn(buildPendingMatch(campaignId));

        matchService.createByInfluencer(campaignId, "otro@influencer.com");

        verify(matchPersistence).createByInfluencer(campaignId, "otro@influencer.com");
    }

    // -------------------------------------------------------------------------
    //  createByBusiness
    // --------------------------------------------------------------------------

    @Test
    void createByBusiness_shouldDelegateToPersistence() {
        UUID influencerId = UUID.randomUUID();
        UUID campaignId   = UUID.randomUUID();
        Match match = buildPendingMatch(campaignId);

        when(matchPersistence.createByBusiness(influencerId, campaignId, "business@test.com")).thenReturn(match);

        matchService.createByBusiness(influencerId, campaignId, "business@test.com");

        verify(matchPersistence).createByBusiness(influencerId, campaignId, "business@test.com");
    }

    @Test
    void createByBusiness_shouldReturnPendingMatch() {
        UUID influencerId = UUID.randomUUID();
        UUID campaignId   = UUID.randomUUID();
        Match match = buildPendingMatch(campaignId);

        when(matchPersistence.createByBusiness(influencerId, campaignId, "business@test.com")).thenReturn(match);

        Match result = matchService.createByBusiness(influencerId, campaignId, "business@test.com");

        assertNotNull(result);
        assertEquals(MatchStatus.PENDING, result.getStatus());
    }

    @Test
    void createByBusiness_shouldReturnCompletedMatchWhenMutual() {
        UUID influencerId = UUID.randomUUID();
        UUID campaignId   = UUID.randomUUID();
        Match completed = buildCompletedMatch(campaignId);

        when(matchPersistence.createByBusiness(influencerId, campaignId, "business@test.com")).thenReturn(completed);

        Match result = matchService.createByBusiness(influencerId, campaignId, "business@test.com");

        assertEquals(MatchStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getMatchedAt());
    }

    @Test
    void createByBusiness_shouldPassExactParamsToPersistence() {
        UUID influencerId = UUID.randomUUID();
        UUID campaignId   = UUID.randomUUID();
        when(matchPersistence.createByBusiness(any(), any(), any())).thenReturn(buildPendingMatch(campaignId));

        matchService.createByBusiness(influencerId, campaignId, "otro@comercio.com");

        verify(matchPersistence).createByBusiness(influencerId, campaignId, "otro@comercio.com");
    }

    @Test
    void createByBusiness_shouldNotCallCreateByInfluencer() {
        UUID influencerId = UUID.randomUUID();
        UUID campaignId   = UUID.randomUUID();
        when(matchPersistence.createByBusiness(any(), any(), any())).thenReturn(buildPendingMatch(campaignId));

        matchService.createByBusiness(influencerId, campaignId, "business@test.com");

        verify(matchPersistence, never()).createByInfluencer(any(), any());
    }

    // ------------------------------------------------------------------------
    //  findByInfluencer
    // -------------------------------------------------------------------------

    @Test
    void findByInfluencer_shouldDelegateToPersistence() {
        UUID campaignId = UUID.randomUUID();
        when(matchPersistence.findByInfluencer(campaignId, "influencer@test.com"))
                .thenReturn(Optional.empty());

        matchService.findByInfluencer(campaignId, "influencer@test.com");

        verify(matchPersistence).findByInfluencer(campaignId, "influencer@test.com");
    }

    @Test
    void findByInfluencer_shouldReturnMatchWhenFound() {
        UUID campaignId = UUID.randomUUID();
        Match match = buildPendingMatch(campaignId);

        when(matchPersistence.findByInfluencer(campaignId, "influencer@test.com"))
                .thenReturn(Optional.of(match));

        Optional<Match> result = matchService.findByInfluencer(campaignId, "influencer@test.com");

        assertTrue(result.isPresent());
        assertEquals(MatchStatus.PENDING, result.get().getStatus());
    }

    @Test
    void findByInfluencer_shouldReturnEmptyWhenNotFound() {
        UUID campaignId = UUID.randomUUID();
        when(matchPersistence.findByInfluencer(campaignId, "influencer@test.com"))
                .thenReturn(Optional.empty());

        Optional<Match> result = matchService.findByInfluencer(campaignId, "influencer@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void findByInfluencer_shouldNotCallCreate() {
        UUID campaignId = UUID.randomUUID();
        when(matchPersistence.findByInfluencer(any(), any())).thenReturn(Optional.empty());

        matchService.findByInfluencer(campaignId, "influencer@test.com");

        verify(matchPersistence, never()).createByInfluencer(any(), any());
    }

    // -------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    private Match buildPendingMatch(UUID campaignId) {
        return Match.builder()
                .id(UUID.randomUUID())
                .campaignId(campaignId)
                .influencerId(UUID.randomUUID())
                .status(MatchStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Match buildCompletedMatch(UUID campaignId) {
        return Match.builder()
                .id(UUID.randomUUID())
                .campaignId(campaignId)
                .influencerId(UUID.randomUUID())
                .businessId(UUID.randomUUID())
                .status(MatchStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .matchedAt(LocalDateTime.now())
                .build();
    }
}
