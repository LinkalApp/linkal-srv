package es.miw.tfm.linkal.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.miw.tfm.linkal.configuration.JwtService;
import es.miw.tfm.linkal.configuration.SecurityConfiguration;
import es.miw.tfm.linkal.domain.exceptions.ForbiddenException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.model.enums.CampaignStatus;
import es.miw.tfm.linkal.domain.services.CampaignService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignResource.class)
@Import(SecurityConfiguration.class)
public class CampaignResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CampaignService campaignService;

    @MockitoBean
    private JwtService jwtService;

    // --------------------------------------------------------------------------
    //  POST /campaigns — crear campaña
    // --------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void create_shouldReturn201WhenDataIsValid() throws Exception {
        Campaign request = buildValidCampaign();
        Campaign saved = buildSavedCampaign();

        when(campaignService.create(any(), eq("business@test.com"))).thenReturn(saved);

        mockMvc.perform(post("/api/campaigns")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Campaña Verano"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void create_shouldReturn401WhenNotAuthenticated() throws Exception {
        Campaign request = buildValidCampaign();

        mockMvc.perform(post("/api/campaigns")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void create_shouldReturn403WhenNotBusiness() throws Exception {
        Campaign request = buildValidCampaign();

        mockMvc.perform(post("/api/campaigns")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unknown@test.com", roles = "BUSINESS")
    void create_shouldReturn404WhenBusinessNotFound() throws Exception {
        Campaign request = buildValidCampaign();

        when(campaignService.create(any(), eq("unknown@test.com")))
                .thenThrow(new NotFoundException("Business not found: unknown@test.com"));

        mockMvc.perform(post("/api/campaigns")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void create_shouldReturnCampaignBodyWithBusinessId() throws Exception {
        UUID businessId = UUID.randomUUID();
        Campaign request = buildValidCampaign();
        Campaign saved = buildSavedCampaign();
        saved.setBusinessId(businessId);

        when(campaignService.create(any(), eq("business@test.com"))).thenReturn(saved);

        mockMvc.perform(post("/api/campaigns")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.businessId").value(businessId.toString()));
    }

    // -------------------------------------------------------------------------
    //  PUT /api/campaigns/{id} — actualizar campaña
    // --------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void update_shouldReturn200WithUpdatedCampaign() throws Exception {
        UUID id = UUID.randomUUID();
        Campaign campaign = buildUpdateCampaign(CampaignStatus.IN_PROGRESS);
        Campaign updated = buildSavedCampaign();
        updated.setTitle("Campaña editada");
        updated.setStatus(CampaignStatus.IN_PROGRESS);

        when(campaignService.update(eq(id), any(), eq("business@test.com"))).thenReturn(updated);

        mockMvc.perform(put("/api/campaigns/" + id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(campaign)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Campaña editada"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void update_shouldReturn200WhenStatusChangedToClosed() throws Exception {
        UUID id = UUID.randomUUID();
        Campaign updated = buildSavedCampaign();
        updated.setStatus(CampaignStatus.CLOSED);

        when(campaignService.update(eq(id), any(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/campaigns/" + id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUpdateCampaign(CampaignStatus.CLOSED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void update_shouldReturn401WhenNotAuthenticated() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/campaigns/" + id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUpdateCampaign(null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void update_shouldReturn403WhenNotBusiness() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/campaigns/" + id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUpdateCampaign(null))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "other@test.com", roles = "BUSINESS")
    void update_shouldReturn403WhenNotOwner() throws Exception {
        UUID id = UUID.randomUUID();

        when(campaignService.update(any(), any(), eq("other@test.com")))
                .thenThrow(new ForbiddenException("No tienes permiso para editar esta campaña"));

        mockMvc.perform(put("/api/campaigns/" + id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUpdateCampaign(null))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void update_shouldReturn404WhenCampaignNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(campaignService.update(any(), any(), any()))
                .thenThrow(new NotFoundException("Campaign not found: " + id));

        mockMvc.perform(put("/api/campaigns/" + id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUpdateCampaign(null))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void update_shouldAcceptEmptyBody() throws Exception {
        UUID id = UUID.randomUUID();
        when(campaignService.update(any(), any(), any())).thenReturn(buildSavedCampaign());

        mockMvc.perform(put("/api/campaigns/" + id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    // --------------------------------------------------------------------------
    //  DELETE /api/campaigns/{id} — eliminar campaña
    // --------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void delete_shouldReturn204WhenSuccessful() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(campaignService).delete(eq(id), eq("business@test.com"));

        mockMvc.perform(delete("/api/campaigns/" + id)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn401WhenNotAuthenticated() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/campaigns/" + id)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void delete_shouldReturn403WhenNotBusiness() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/campaigns/" + id)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "other@test.com", roles = "BUSINESS")
    void delete_shouldReturn403WhenNotOwner() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new ForbiddenException("No tienes permiso para eliminar esta campaña"))
                .when(campaignService).delete(eq(id), eq("other@test.com"));

        mockMvc.perform(delete("/api/campaigns/" + id)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void delete_shouldReturn404WhenCampaignNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new NotFoundException("Campaign not found: " + id))
                .when(campaignService).delete(eq(id), eq("business@test.com"));

        mockMvc.perform(delete("/api/campaigns/" + id)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // --------------------------------------------------------------------------
    //  helpers
    // --------------------------------------------------------------------------

    private Campaign buildValidCampaign() {
        Campaign campaign = new Campaign();
        campaign.setTitle("Campaña Verano");
        campaign.setDescription("Descripción de la campaña para este verano");
        campaign.setRequirements("Mínimo 500 seguidores en Instagram");
        campaign.setReward("Descuento del 20% en tienda");
        campaign.setObjective("Aumentar visibilidad de la marca");
        return campaign;
    }

    private Campaign buildSavedCampaign() {
        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setTitle("Campaña Verano");
        campaign.setDescription("Descripción de la campaña para este verano");
        campaign.setRequirements("Mínimo 500 seguidores en Instagram");
        campaign.setReward("Descuento del 20% en tienda");
        campaign.setObjective("Aumentar visibilidad de la marca");
        campaign.setStatus(CampaignStatus.OPEN);
        campaign.setCreationDate(LocalDate.now());
        return campaign;
    }

    private Campaign buildUpdateCampaign(CampaignStatus status) {
        Campaign campaign = new Campaign();
        campaign.setTitle("Campaña editada");
        campaign.setDescription("Descripción actualizada");
        campaign.setRequirements("Nuevos requisitos");
        campaign.setReward("Nueva recompensa");
        campaign.setObjective("Nuevo objetivo");
        campaign.setStatus(status);
        return campaign;
    }
}
