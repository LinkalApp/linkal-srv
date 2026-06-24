package es.miw.tfm.linkal.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.miw.tfm.linkal.configuration.JwtService;
import es.miw.tfm.linkal.configuration.SecurityConfiguration;
import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.exceptions.ForbiddenException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Evaluation;
import es.miw.tfm.linkal.domain.services.EvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvaluationResource.class)
@Import(SecurityConfiguration.class)
class EvaluationResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EvaluationService evaluationService;

    @MockitoBean
    private JwtService jwtService;

    // ------------------------------------------------------------------------
    //  POST /api/evaluations/matches/{matchId} — crear valoración
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void create_shouldReturn201WhenDataIsValid() throws Exception {
        UUID matchId = UUID.randomUUID();
        Evaluation request = buildEvaluationRequest(5);
        Evaluation saved = buildSavedEvaluation(matchId, 5);

        when(evaluationService.create(any(), eq(matchId), eq("business@test.com"))).thenReturn(saved);

        mockMvc.perform(post("/api/evaluations/matches/{matchId}", matchId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.score").value(5));
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void create_shouldReturn201WithMinScore() throws Exception {
        UUID matchId = UUID.randomUUID();
        Evaluation saved = buildSavedEvaluation(matchId, 1);
        when(evaluationService.create(any(), any(), any())).thenReturn(saved);

        mockMvc.perform(post("/api/evaluations/matches/{matchId}", matchId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildEvaluationRequest(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(1));
    }

    @Test
    void create_shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/evaluations/matches/{matchId}", UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildEvaluationRequest(4))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void create_shouldReturn403WhenNotBusiness() throws Exception {
        mockMvc.perform(post("/api/evaluations/matches/{matchId}", UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildEvaluationRequest(4))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "other@test.com", roles = "BUSINESS")
    void create_shouldReturn403WhenForbiddenException() throws Exception {
        UUID matchId = UUID.randomUUID();
        when(evaluationService.create(any(), any(), eq("other@test.com")))
                .thenThrow(new ForbiddenException("No tienes permiso para valorar este match"));

        mockMvc.perform(post("/api/evaluations/matches/{matchId}", matchId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildEvaluationRequest(4))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void create_shouldReturn404WhenMatchNotFound() throws Exception {
        UUID matchId = UUID.randomUUID();
        when(evaluationService.create(any(), eq(matchId), any()))
                .thenThrow(new NotFoundException("Match not found: " + matchId));

        mockMvc.perform(post("/api/evaluations/matches/{matchId}", matchId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildEvaluationRequest(4))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void create_shouldReturn404WhenBusinessNotFound() throws Exception {
        when(evaluationService.create(any(), any(), any()))
                .thenThrow(new NotFoundException("Business not found"));

        mockMvc.perform(post("/api/evaluations/matches/{matchId}", UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildEvaluationRequest(4))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void create_shouldReturn409WhenAlreadyRated() throws Exception {
        when(evaluationService.create(any(), any(), any()))
                .thenThrow(new ConflictException("Ya has valorado al influencer de este match"));

        mockMvc.perform(post("/api/evaluations/matches/{matchId}", UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildEvaluationRequest(4))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void create_shouldReturn409WhenCampaignNotClosed() throws Exception {
        when(evaluationService.create(any(), any(), any()))
                .thenThrow(new ConflictException("La campaña debe estar CLOSED para poder valorar"));

        mockMvc.perform(post("/api/evaluations/matches/{matchId}", UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildEvaluationRequest(4))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void create_shouldReturnValuedUserIdInBody() throws Exception {
        UUID matchId = UUID.randomUUID();
        UUID influencerId = UUID.randomUUID();
        Evaluation saved = buildSavedEvaluation(matchId, 5);
        saved.setValuedUserId(influencerId);

        when(evaluationService.create(any(), any(), any())).thenReturn(saved);

        mockMvc.perform(post("/api/evaluations/matches/{matchId}", matchId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildEvaluationRequest(5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valuedUserId").value(influencerId.toString()));
    }

    // ------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    private Evaluation buildEvaluationRequest(int score) {
        return Evaluation.builder().score(score).build();
    }

    private Evaluation buildSavedEvaluation(UUID matchId, int score) {
        return Evaluation.builder()
                .id(UUID.randomUUID())
                .score(score)
                .valuedUserId(UUID.randomUUID())
                .matchId(matchId)
                .build();
    }
}
