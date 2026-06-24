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
import java.util.List;
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
    //  findPendingByInfluencer
    // -------------------------------------------------------------------------

    @Test
    void findPendingByInfluencer_shouldDelegateToPersistence() {
        when(matchPersistence.findPendingByInfluencer("influencer@test.com"))
                .thenReturn(java.util.List.of());

        matchService.findPendingByInfluencer("influencer@test.com");

        verify(matchPersistence).findPendingByInfluencer("influencer@test.com");
    }

    @Test
    void findPendingByInfluencer_shouldReturnEmptyList_whenNoMatches() {
        when(matchPersistence.findPendingByInfluencer("influencer@test.com"))
                .thenReturn(java.util.List.of());

        java.util.List<Match> result = matchService.findPendingByInfluencer("influencer@test.com");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findPendingByInfluencer_shouldReturnMatches() {
        UUID campaignId = UUID.randomUUID();
        when(matchPersistence.findPendingByInfluencer("influencer@test.com"))
                .thenReturn(java.util.List.of(buildPendingMatch(campaignId)));

        java.util.List<Match> result = matchService.findPendingByInfluencer("influencer@test.com");

        assertEquals(1, result.size());
        assertEquals(MatchStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void findPendingByInfluencer_shouldNotCallFindPendingByBusiness() {
        when(matchPersistence.findPendingByInfluencer(any())).thenReturn(java.util.List.of());

        matchService.findPendingByInfluencer("influencer@test.com");

        verify(matchPersistence, never()).findPendingByBusiness(any());
    }

    // -------------------------------------------------------------------------
    //  findPendingByBusiness
    // -------------------------------------------------------------------------

    @Test
    void findPendingByBusiness_shouldDelegateToPersistence() {
        when(matchPersistence.findPendingByBusiness("business@test.com"))
                .thenReturn(java.util.List.of());

        matchService.findPendingByBusiness("business@test.com");

        verify(matchPersistence).findPendingByBusiness("business@test.com");
    }

    @Test
    void findPendingByBusiness_shouldReturnEmptyList_whenNoMatches() {
        when(matchPersistence.findPendingByBusiness("business@test.com"))
                .thenReturn(java.util.List.of());

        java.util.List<Match> result = matchService.findPendingByBusiness("business@test.com");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findPendingByBusiness_shouldReturnMatches() {
        UUID campaignId = UUID.randomUUID();
        when(matchPersistence.findPendingByBusiness("business@test.com"))
                .thenReturn(java.util.List.of(buildPendingMatch(campaignId)));

        java.util.List<Match> result = matchService.findPendingByBusiness("business@test.com");

        assertEquals(1, result.size());
        assertEquals(MatchStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void findPendingByBusiness_shouldNotCallFindPendingByInfluencer() {
        when(matchPersistence.findPendingByBusiness(any())).thenReturn(java.util.List.of());

        matchService.findPendingByBusiness("business@test.com");

        verify(matchPersistence, never()).findPendingByInfluencer(any());
    }

    // findCompletedByInfluencer ---------------------------------------------------------------------

    @Test
    void findCompletedByInfluencer_shouldDelegateToPersistence() {
        when(matchPersistence.findCompletedByInfluencer("influencer@test.com")).thenReturn(List.of());
        matchService.findCompletedByInfluencer("influencer@test.com");
        verify(matchPersistence).findCompletedByInfluencer("influencer@test.com");
    }

    @Test
    void findCompletedByInfluencer_shouldReturnEmptyList() {
        when(matchPersistence.findCompletedByInfluencer("influencer@test.com")).thenReturn(List.of());
        assertTrue(matchService.findCompletedByInfluencer("influencer@test.com").isEmpty());
    }

    @Test
    void findCompletedByInfluencer_shouldReturnMatches() {
        UUID id = UUID.randomUUID();
        when(matchPersistence.findCompletedByInfluencer("influencer@test.com")).thenReturn(List.of(buildCompletedMatch(id)));
        List<Match> result = matchService.findCompletedByInfluencer("influencer@test.com");
        assertEquals(1, result.size());
        assertEquals(MatchStatus.COMPLETED, result.get(0).getStatus());
    }

    @Test
    void findCompletedByInfluencer_shouldNotCallFindCompletedByBusiness() {
        when(matchPersistence.findCompletedByInfluencer(any())).thenReturn(List.of());
        matchService.findCompletedByInfluencer("influencer@test.com");
        verify(matchPersistence, never()).findCompletedByBusiness(any());
    }

    // findCompletedByBusiness --------------------------------------------------------------------------

    @Test
    void findCompletedByBusiness_shouldDelegateToPersistence() {
        when(matchPersistence.findCompletedByBusiness("business@test.com")).thenReturn(List.of());
        matchService.findCompletedByBusiness("business@test.com");
        verify(matchPersistence).findCompletedByBusiness("business@test.com");
    }

    @Test
    void findCompletedByBusiness_shouldReturnEmptyList() {
        when(matchPersistence.findCompletedByBusiness("business@test.com")).thenReturn(List.of());
        assertTrue(matchService.findCompletedByBusiness("business@test.com").isEmpty());
    }

    @Test
    void findCompletedByBusiness_shouldReturnMatches() {
        UUID id = UUID.randomUUID();
        when(matchPersistence.findCompletedByBusiness("business@test.com")).thenReturn(List.of(buildCompletedMatch(id)));
        List<Match> result = matchService.findCompletedByBusiness("business@test.com");
        assertEquals(1, result.size());
        assertEquals(MatchStatus.COMPLETED, result.get(0).getStatus());
    }

    @Test
    void findCompletedByBusiness_shouldNotCallFindCompletedByInfluencer() {
        when(matchPersistence.findCompletedByBusiness(any())).thenReturn(List.of());
        matchService.findCompletedByBusiness("business@test.com");
        verify(matchPersistence, never()).findCompletedByInfluencer(any());
    }

    // -------------------------------------------------------------------------
    //  findCompletedByCampaign
    // -------------------------------------------------------------------------

    @Test
    void findCompletedByCampaign_shouldDelegateToPersistence() {
        UUID campaignId = UUID.randomUUID();
        when(matchPersistence.findCompletedByCampaign(campaignId, "business@test.com")).thenReturn(List.of());
        matchService.findCompletedByCampaign(campaignId, "business@test.com");
        verify(matchPersistence).findCompletedByCampaign(campaignId, "business@test.com");
    }

    @Test
    void findCompletedByCampaign_shouldReturnEmptyList() {
        UUID campaignId = UUID.randomUUID();
        when(matchPersistence.findCompletedByCampaign(campaignId, "business@test.com")).thenReturn(List.of());
        assertTrue(matchService.findCompletedByCampaign(campaignId, "business@test.com").isEmpty());
    }

    @Test
    void findCompletedByCampaign_shouldReturnMatches() {
        UUID campaignId = UUID.randomUUID();
        when(matchPersistence.findCompletedByCampaign(campaignId, "business@test.com"))
                .thenReturn(List.of(buildCompletedMatch(campaignId)));
        List<Match> result = matchService.findCompletedByCampaign(campaignId, "business@test.com");
        assertEquals(1, result.size());
        assertEquals(MatchStatus.COMPLETED, result.get(0).getStatus());
        assertEquals(campaignId, result.get(0).getCampaignId());
    }

    @Test
    void findCompletedByCampaign_shouldPassAllParamsToPersistence() {
        UUID campaignId = UUID.randomUUID();
        when(matchPersistence.findCompletedByCampaign(any(), any())).thenReturn(List.of());
        matchService.findCompletedByCampaign(campaignId, "otro@empresa.com");
        verify(matchPersistence).findCompletedByCampaign(eq(campaignId), eq("otro@empresa.com"));
    }

    @Test
    void findCompletedByCampaign_shouldNotCallFindCompletedByBusiness() {
        UUID campaignId = UUID.randomUUID();
        when(matchPersistence.findCompletedByCampaign(any(), any())).thenReturn(List.of());
        matchService.findCompletedByCampaign(campaignId, "business@test.com");
        verify(matchPersistence, never()).findCompletedByBusiness(any());
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
