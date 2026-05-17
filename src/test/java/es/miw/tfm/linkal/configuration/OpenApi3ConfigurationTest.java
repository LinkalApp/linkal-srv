package es.miw.tfm.linkal.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OpenApi3ConfigurationTest {

    @Test
    void openAPI_shouldReturnNonNullInstance() {
        OpenApi3Configuration config = new OpenApi3Configuration();

        OpenAPI openAPI = config.openAPI();

        assertNotNull(openAPI);
    }

    @Test
    void openAPI_shouldHaveCorrectTitle() {
        OpenApi3Configuration config = new OpenApi3Configuration();

        OpenAPI openAPI = config.openAPI();

        assertEquals("Linkal API", openAPI.getInfo().getTitle());
    }

    @Test
    void openAPI_shouldHaveCorrectVersion() {
        OpenApi3Configuration config = new OpenApi3Configuration();

        OpenAPI openAPI = config.openAPI();

        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void openAPI_shouldHaveBearerAuthSecurityScheme() {
        OpenApi3Configuration config = new OpenApi3Configuration();

        OpenAPI openAPI = config.openAPI();

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertNotNull(scheme);
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
    }
}
