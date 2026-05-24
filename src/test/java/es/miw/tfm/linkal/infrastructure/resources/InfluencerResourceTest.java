package es.miw.tfm.linkal.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.miw.tfm.linkal.configuration.JwtService;
import es.miw.tfm.linkal.configuration.SecurityConfiguration;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Influencer;
import es.miw.tfm.linkal.domain.services.InfluencerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InfluencerResource.class)
@Import(SecurityConfiguration.class)
class InfluencerResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InfluencerService influencerService;

    @MockitoBean
    private JwtService jwtService;

    // -------------------------------------------------------------------------
    //  POST /influencers — registro (público)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    void create_shouldReturn201WhenDataIsValid() throws Exception {
        Influencer influencer = buildInfluencer();
        when(influencerService.create(any())).thenReturn(influencer);

        mockMvc.perform(post("/api/influencers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(influencer)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void create_shouldReturn400WhenEmailIsInvalid() throws Exception {
        Influencer influencer = buildInfluencer();
        influencer.setEmail("not-an-email");

        mockMvc.perform(post("/api/influencers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(influencer)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void create_shouldReturn400WhenPasswordIsBlank() throws Exception {
        Influencer influencer = buildInfluencer();
        influencer.setPassword("");

        mockMvc.perform(post("/api/influencers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(influencer)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------------
    //  GET /influencers/me — solo INFLUENCER autenticado
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void readMe_shouldReturn200WithProfile() throws Exception {
        Influencer influencer = buildInfluencer();

        when(influencerService.readMe("influencer@test.com")).thenReturn(influencer);

        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("influencer@test.com"))
                .andExpect(jsonPath("$.artisticName").value("TestArtist"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void readMe_shouldReturnAverageRatingWhenPresent() throws Exception {
        Influencer influencer = buildInfluencer();
        influencer.setAverageRating(4.2);

        when(influencerService.readMe("influencer@test.com")).thenReturn(influencer);

        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.2));
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void readMe_shouldNotIncludeAverageRatingWhenNull() throws Exception {
        Influencer influencer = buildInfluencer();

        when(influencerService.readMe("influencer@test.com")).thenReturn(influencer);

        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").doesNotExist());
    }

    @Test
    void readMe_shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void readMe_shouldReturn403WhenNotInfluencer() throws Exception {
        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unknown@test.com", roles = "INFLUENCER")
    void readMe_shouldReturn404WhenInfluencerNotFound() throws Exception {
        when(influencerService.readMe("unknown@test.com"))
                .thenThrow(new NotFoundException("Influencer not found: unknown@test.com"));

        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------------
    //  PUT /api/influencers/me — solo INFLUENCER autenticado
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void updateMe_shouldReturn200WithUpdatedProfile() throws Exception {
        Influencer updated = buildInfluencer();
        updated.setArtisticName("NewArtist");
        updated.setInstagram("@new_ig");

        when(influencerService.updateMe(eq("influencer@test.com"), any())).thenReturn(updated);

        mockMvc.perform(put("/api/influencers/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artisticName").value("NewArtist"))
                .andExpect(jsonPath("$.instagram").value("@new_ig"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void updateMe_shouldReturn200AndNotExposePassword() throws Exception {
        Influencer updated = buildInfluencer();
        updated.setPassword(null); // el servicio siempre borra la password

        when(influencerService.updateMe(eq("influencer@test.com"), any())).thenReturn(updated);

        mockMvc.perform(put("/api/influencers/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildInfluencer())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void updateMe_shouldReturn200WithAverageRating() throws Exception {
        Influencer updated = buildInfluencer();
        updated.setAverageRating(4.5);

        when(influencerService.updateMe(eq("influencer@test.com"), any())).thenReturn(updated);

        mockMvc.perform(put("/api/influencers/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildInfluencer())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }

    @Test
    void updateMe_shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/influencers/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildInfluencer())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void updateMe_shouldReturn403WhenNotInfluencer() throws Exception {
        mockMvc.perform(put("/api/influencers/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildInfluencer())))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    //  DELETE /api/influencers/me — solo INFLUENCER autenticado
    // --------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void deleteMe_shouldReturn204WhenInfluencerAuthenticated() throws Exception {
        doNothing().when(influencerService).deleteMe("influencer@test.com");

        mockMvc.perform(delete("/api/influencers/me").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void deleteMe_shouldReturn404WhenInfluencerNotFound() throws Exception {
        doThrow(new NotFoundException("Influencer not found"))
                .when(influencerService).deleteMe("influencer@test.com");

        mockMvc.perform(delete("/api/influencers/me").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMe_shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/influencers/me").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void deleteMe_shouldReturn403WhenNotInfluencer() throws Exception {
        mockMvc.perform(delete("/api/influencers/me").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    private Influencer buildInfluencer() {
        Influencer influencer = new Influencer();
        influencer.setName("Test Influencer");
        influencer.setEmail("influencer@test.com");
        influencer.setPassword("password123");
        influencer.setArtisticName("TestArtist");
        return influencer;
    }
}
