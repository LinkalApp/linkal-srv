package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.configuration.JwtService;
import es.miw.tfm.linkal.configuration.SecurityConfiguration;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.AdminUserDetail;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import es.miw.tfm.linkal.domain.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminResource.class)
@Import(SecurityConfiguration.class)
public class AdminResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    // -------------------------------------------------------------------------
    //  GET /api/admin/users
    // --------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "admin@linkal.es", roles = "ADMIN")
    void findAll_shouldReturn200WithListWhenAdmin() throws Exception {
        List<AdminUserDetail> users = Arrays.asList(
                buildDetail(UUID.randomUUID(), "Ana", RoleType.INFLUENCER),
                buildDetail(UUID.randomUUID(), "Nike", RoleType.BUSINESS));

        when(userService.findAll(null, null)).thenReturn(users);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Ana"))
                .andExpect(jsonPath("$[1].name").value("Nike"));
    }

    @Test
    void findAll_shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void findAll_shouldReturn403WhenInfluencer() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void findAll_shouldReturn403WhenBusiness() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@linkal.es", roles = "ADMIN")
    void findAll_withRoleFilter_passesRoleToService() throws Exception {
        when(userService.findAll(eq(RoleType.INFLUENCER), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/users").param("role", "INFLUENCER"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@linkal.es", roles = "ADMIN")
    void findAll_withVerifiedFilter_passesVerifiedToService() throws Exception {
        when(userService.findAll(any(), eq(true))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/users").param("verified", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@linkal.es", roles = "ADMIN")
    void findAll_withBothFilters_passesFiltersToService() throws Exception {
        when(userService.findAll(eq(RoleType.BUSINESS), eq(false))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/users")
                        .param("role", "BUSINESS")
                        .param("verified", "false"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@linkal.es", roles = "ADMIN")
    void findAll_whenEmpty_returnsEmptyArray() throws Exception {
        when(userService.findAll(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "admin@linkal.es", roles = "ADMIN")
    void findAll_influencerDetailHasArtisticName() throws Exception {
        AdminUserDetail detail = buildDetail(UUID.randomUUID(), "Ana", RoleType.INFLUENCER);
        detail.setArtisticName("AnaFashion");
        when(userService.findAll(any(), any())).thenReturn(List.of(detail));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].artisticName").value("AnaFashion"));
    }

    // -------------------------------------------------------------------------
    //  GET /api/admin/users/{id}
    // --------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "admin@linkal.es", roles = "ADMIN")
    void findById_shouldReturn200WithUserDetail() throws Exception {
        UUID id = UUID.randomUUID();
        AdminUserDetail detail = buildDetail(id, "Carlos", RoleType.BUSINESS);
        detail.setCategory("Restauración");

        when(userService.findById(id)).thenReturn(detail);

        mockMvc.perform(get("/api/admin/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Carlos"))
                .andExpect(jsonPath("$.category").value("Restauración"));
    }

    @Test
    void findById_shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/users/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void findById_shouldReturn403WhenInfluencer() throws Exception {
        mockMvc.perform(get("/api/admin/users/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void findById_shouldReturn403WhenBusiness() throws Exception {
        mockMvc.perform(get("/api/admin/users/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@linkal.es", roles = "ADMIN")
    void findById_shouldReturn404WhenUserNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.findById(id)).thenThrow(new NotFoundException("User not found: " + id));

        mockMvc.perform(get("/api/admin/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@linkal.es", roles = "ADMIN")
    void findById_influencerDetailHasInfluencerFields() throws Exception {
        UUID id = UUID.randomUUID();
        AdminUserDetail detail = buildDetail(id, "Lucia", RoleType.INFLUENCER);
        detail.setArtisticName("LuciaModa");
        detail.setInstagram("@lucia");
        detail.setInterests(Arrays.asList("Moda", "Belleza"));

        when(userService.findById(id)).thenReturn(detail);

        mockMvc.perform(get("/api/admin/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artisticName").value("LuciaModa"))
                .andExpect(jsonPath("$.instagram").value("@lucia"))
                .andExpect(jsonPath("$.interests.length()").value(2));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  helpers
    // ─────────────────────────────────────────────────────────────────────────

    private AdminUserDetail buildDetail(UUID id, String name, RoleType role) {
        return AdminUserDetail.builder()
                .id(id)
                .name(name)
                .email(name.toLowerCase() + "@test.com")
                .verified(true)
                .role(role)
                .build();
    }
}