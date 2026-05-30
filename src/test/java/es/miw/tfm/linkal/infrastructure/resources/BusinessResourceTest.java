package es.miw.tfm.linkal.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.miw.tfm.linkal.configuration.JwtService;
import es.miw.tfm.linkal.configuration.SecurityConfiguration;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Business;
import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.model.enums.CampaignStatus;
import es.miw.tfm.linkal.domain.services.BusinessService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BusinessResource.class)
@Import(SecurityConfiguration.class)
public class BusinessResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BusinessService businessService;
    @MockitoBean
    private CampaignService campaignService;

    @MockitoBean
    private JwtService jwtService;

    // -------------------------------------------------------------------------
    //  POST /businesses — registro (público)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void create_shouldReturn201WhenDataIsValid() throws Exception {
        Business business = buildBusiness();
        when(businessService.create(any())).thenReturn(business);

        mockMvc.perform(post("/api/businesses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(business)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void create_shouldReturn400WhenEmailIsInvalid() throws Exception {
        Business business = buildBusiness();
        business.setEmail("invalidemail");

        mockMvc.perform(post("/api/businesses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(business)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void create_shouldReturn400WhenPasswordIsBlank() throws Exception {
        Business business = buildBusiness();
        business.setPassword("");

        mockMvc.perform(post("/api/businesses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(business)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    //  GET /businesses/me — solo BUSINESS autenticado
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void readMe_shouldReturn200WithProfile() throws Exception {
        Business business = buildBusiness();

        when(businessService.readMe("business@test.com")).thenReturn(business);

        mockMvc.perform(get("/api/businesses/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("business@test.com"))
                .andExpect(jsonPath("$.address").value("Calle Mayor 1"))
                .andExpect(jsonPath("$.province").value("Madrid"))
                .andExpect(jsonPath("$.website").value("https://miempresa.com"))
                .andExpect(jsonPath("$.category").value("Moda"));
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void readMe_shouldReturn200AndNotExposePassword() throws Exception {
        Business business = buildBusiness();
        business.setPassword(null); // el servicio siempre borra la password

        when(businessService.readMe("business@test.com")).thenReturn(business);

        mockMvc.perform(get("/api/businesses/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void readMe_shouldReturnAverageRatingWhenPresent() throws Exception {
        Business business = buildBusiness();
        business.setAverageRating(3.8);

        when(businessService.readMe("business@test.com")).thenReturn(business);

        mockMvc.perform(get("/api/businesses/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(3.8));
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void readMe_shouldNotIncludeAverageRatingWhenNull() throws Exception {
        Business business = buildBusiness();
        // averageRating es null por defecto

        when(businessService.readMe("business@test.com")).thenReturn(business);

        mockMvc.perform(get("/api/businesses/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").doesNotExist());
    }

    @Test
    void readMe_shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/businesses/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void readMe_shouldReturn403WhenNotBusiness() throws Exception {
        mockMvc.perform(get("/api/businesses/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unknown@test.com", roles = "BUSINESS")
    void readMe_shouldReturn404WhenBusinessNotFound() throws Exception {
        when(businessService.readMe("unknown@test.com"))
                .thenThrow(new NotFoundException("Business not found: unknown@test.com"));

        mockMvc.perform(get("/api/businesses/me"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    //  PUT /businesses/me — solo BUSINESS autenticado
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void updateMe_shouldReturn200WhenAuthenticated() throws Exception {
        Business business = buildBusiness();

        when(businessService.updateMe(eq("business@test.com"), any())).thenReturn(business);

        mockMvc.perform(put("/api/businesses/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(business)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("business@test.com"))
                .andExpect(jsonPath("$.address").value("Calle Mayor 1"))
                .andExpect(jsonPath("$.website").value("https://miempresa.com"));
    }

    @Test
    void updateMe_shouldReturn401WhenNotAuthenticated() throws Exception {
        Business business = buildBusiness();

        mockMvc.perform(put("/api/businesses/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(business)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void updateMe_shouldReturn403WhenNotBusiness() throws Exception {
        Business business = buildBusiness();

        mockMvc.perform(put("/api/businesses/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(business)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unknown@test.com", roles = "BUSINESS")
    void updateMe_shouldReturn404WhenBusinessNotFound() throws Exception {
        Business business = buildBusiness();

        when(businessService.updateMe(eq("unknown@test.com"), any()))
                .thenThrow(new NotFoundException("Business not found: unknown@test.com"));

        mockMvc.perform(put("/api/businesses/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(business)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    //  DELETE /businesses/me — solo BUSINESS autenticado
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void deleteMe_shouldReturn204WhenAuthenticated() throws Exception {
        doNothing().when(businessService).deleteMe("business@test.com");

        mockMvc.perform(delete("/api/businesses/me").with(csrf()))
                .andExpect(status().isNoContent());

        verify(businessService).deleteMe("business@test.com");
    }

    @Test
    void deleteMe_shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/businesses/me").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void deleteMe_shouldReturn403WhenNotBusiness() throws Exception {
        mockMvc.perform(delete("/api/businesses/me").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unknown@test.com", roles = "BUSINESS")
    void deleteMe_shouldReturn404WhenBusinessNotFound() throws Exception {
        doThrow(new NotFoundException("Business not found: unknown@test.com"))
                .when(businessService).deleteMe("unknown@test.com");

        mockMvc.perform(delete("/api/businesses/me").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    //  GET /businesses/{id}/campaigns
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void getCampaigns_shouldReturn200WithCampaignList() throws Exception {
        UUID businessId = UUID.randomUUID();
        Campaign c1 = buildCampaign("Campaña Verano");
        Campaign c2 = buildCampaign("Campaña Invierno");

        when(campaignService.findByBusinessId(businessId)).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/businesses/{id}/campaigns", businessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Campaña Verano"))
                .andExpect(jsonPath("$[1].title").value("Campaña Invierno"));
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void getCampaigns_shouldReturn200WithEmptyList() throws Exception {
        UUID businessId = UUID.randomUUID();
        when(campaignService.findByBusinessId(businessId)).thenReturn(List.of());

        mockMvc.perform(get("/api/businesses/{id}/campaigns", businessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCampaigns_shouldReturn401WhenNotAuthenticated() throws Exception {
        UUID businessId = UUID.randomUUID();

        mockMvc.perform(get("/api/businesses/{id}/campaigns", businessId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void getCampaigns_shouldReturn200ForInfluencer() throws Exception {
        UUID businessId = UUID.randomUUID();
        when(campaignService.findByBusinessId(businessId)).thenReturn(List.of());

        mockMvc.perform(get("/api/businesses/{id}/campaigns", businessId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void getCampaigns_shouldDelegateToServiceWithCorrectId() throws Exception {
        UUID businessId = UUID.randomUUID();
        when(campaignService.findByBusinessId(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/businesses/{id}/campaigns", businessId))
                .andExpect(status().isOk());

        verify(campaignService).findByBusinessId(businessId);
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void getCampaigns_shouldReturnCampaignFieldsCorrectly() throws Exception {
        UUID businessId = UUID.randomUUID();
        Campaign c = buildCampaign("Campaña Test");
        c.setStatus(CampaignStatus.OPEN);
        c.setReward("Descuento 20%");

        when(campaignService.findByBusinessId(businessId)).thenReturn(List.of(c));

        mockMvc.perform(get("/api/businesses/{id}/campaigns", businessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Campaña Test"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].reward").value("Descuento 20%"));
    }

    // -------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    private Business buildBusiness() {
        Business business = new Business();
        business.setName("Test Business");
        business.setEmail("business@test.com");
        business.setPassword("password123");
        business.setAddress("Calle Mayor 1");
        business.setWebsite("https://miempresa.com");
        business.setProvince("Madrid");
        business.setCategory("Moda");
        return business;
    }

    private Campaign buildCampaign(String title) {
        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setTitle(title);
        campaign.setDescription("Descripción de prueba");
        campaign.setRequirements("500 seguidores mínimo");
        campaign.setReward("Descuento 20%");
        campaign.setObjective("Aumentar ventas");
        campaign.setStatus(CampaignStatus.OPEN);
        campaign.setCreationDate(LocalDate.now());
        return campaign;
    }

}