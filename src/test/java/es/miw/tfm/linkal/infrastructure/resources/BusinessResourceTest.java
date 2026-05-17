package es.miw.tfm.linkal.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.miw.tfm.linkal.configuration.JwtService;
import es.miw.tfm.linkal.domain.model.Business;
import es.miw.tfm.linkal.domain.services.BusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BusinessResource.class)
public class BusinessResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BusinessService businessService;

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

}