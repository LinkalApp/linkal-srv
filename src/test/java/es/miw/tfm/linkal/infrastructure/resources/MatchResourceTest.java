package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.configuration.JwtService;
import es.miw.tfm.linkal.configuration.SecurityConfiguration;
import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Match;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import es.miw.tfm.linkal.domain.services.MatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchResource.class)
@Import(SecurityConfiguration.class)
public class MatchResourceTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchService matchService;

    @MockitoBean
    private JwtService jwtService;

    // ---------------------------------------------------------------------------
    //  POST /api/matches/campaigns/{campaignId}
    // --------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void createByInfluencer_shouldReturn201WithPendingMatch() throws Exception {
        UUID campaignId = UUID.randomUUID();
        Match match = buildPendingMatch(campaignId);

        when(matchService.createByInfluencer(eq(campaignId), eq("influencer@test.com"))).thenReturn(match);

        mockMvc.perform(post("/api/matches/campaigns/" + campaignId).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.campaignId").value(campaignId.toString()));
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void createByInfluencer_shouldReturn201WithCompletedMatch_whenMutual() throws Exception {
        UUID campaignId = UUID.randomUUID();
        Match match = buildCompletedMatch(campaignId);

        when(matchService.createByInfluencer(eq(campaignId), eq("influencer@test.com"))).thenReturn(match);

        mockMvc.perform(post("/api/matches/campaigns/" + campaignId).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.matchedAt").exists());
    }

    @Test
    void createByInfluencer_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/matches/campaigns/" + UUID.randomUUID()).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void createByInfluencer_shouldReturn403_whenNotInfluencer() throws Exception {
        mockMvc.perform(post("/api/matches/campaigns/" + UUID.randomUUID()).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void createByInfluencer_shouldReturn409_whenDuplicate() throws Exception {
        UUID campaignId = UUID.randomUUID();
        when(matchService.createByInfluencer(any(), any()))
                .thenThrow(new ConflictException("Ya has expresado interés en esta campaña"));

        mockMvc.perform(post("/api/matches/campaigns/" + campaignId).with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void createByInfluencer_shouldReturn404_whenCampaignNotFound() throws Exception {
        UUID campaignId = UUID.randomUUID();
        when(matchService.createByInfluencer(any(), any()))
                .thenThrow(new NotFoundException("Campaign not found: " + campaignId));

        mockMvc.perform(post("/api/matches/campaigns/" + campaignId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------------------------
    //  GET /api/matches/campaigns/{campaignId}/influencer - obtener las campañas de un influencer
    // --------------------------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void findByInfluencer_shouldReturn200_whenMatchExists() throws Exception {
        UUID campaignId = UUID.randomUUID();
        Match match = buildPendingMatch(campaignId);

        when(matchService.findByInfluencer(eq(campaignId), eq("influencer@test.com")))
                .thenReturn(Optional.of(match));

        mockMvc.perform(get("/api/matches/campaigns/" + campaignId + "/influencer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.campaignId").value(campaignId.toString()));
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void findByInfluencer_shouldReturn200WithCompleted_whenMatchIsCompleted() throws Exception {
        UUID campaignId = UUID.randomUUID();
        Match match = buildCompletedMatch(campaignId);

        when(matchService.findByInfluencer(eq(campaignId), eq("influencer@test.com")))
                .thenReturn(Optional.of(match));

        mockMvc.perform(get("/api/matches/campaigns/" + campaignId + "/influencer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void findByInfluencer_shouldReturn404_whenNoMatchExists() throws Exception {
        UUID campaignId = UUID.randomUUID();
        when(matchService.findByInfluencer(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/matches/campaigns/" + campaignId + "/influencer"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByInfluencer_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/matches/campaigns/" + UUID.randomUUID() + "/influencer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void findByInfluencer_shouldReturn403_whenNotInfluencer() throws Exception {
        mockMvc.perform(get("/api/matches/campaigns/" + UUID.randomUUID() + "/influencer"))
                .andExpect(status().isForbidden());
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
