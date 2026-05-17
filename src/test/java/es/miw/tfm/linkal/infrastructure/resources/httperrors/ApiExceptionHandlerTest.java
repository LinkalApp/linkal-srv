package es.miw.tfm.linkal.infrastructure.resources.httperrors;

import es.miw.tfm.linkal.domain.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
    }

    @Test
    void handleNotFoundException_shouldReturn404() {
        NotFoundException ex = new NotFoundException("Usuario no encontrado");

        ErrorMessage result = handler.handleNotFoundException(ex);

        assertEquals(404, result.getCode());
        assertNotNull(result.getMessage());
    }

    @Test
    void handleBadRequestException_shouldReturn400() {
        BadRequestException ex = new BadRequestException("Credenciales inválidas");

        ErrorMessage result = handler.handleBadRequestException(ex);

        assertEquals(400, result.getCode());
        assertNotNull(result.getMessage());
    }

    @Test
    void handleConflictException_shouldReturn409() {
        ConflictException ex = new ConflictException("El email ya existe");

        ErrorMessage result = handler.handleConflictException(ex);

        assertEquals(409, result.getCode());
        assertNotNull(result.getMessage());
    }

    @Test
    void handleForbiddenException_shouldReturn403() {
        ForbiddenException ex = new ForbiddenException("Acceso denegado");

        ErrorMessage result = handler.handleForbiddenException(ex);

        assertEquals(403, result.getCode());
        assertNotNull(result.getMessage());
    }

    @Test
    void handleBadGatewayException_shouldReturn502() {
        BadGatewayException ex = new BadGatewayException("Servicio externo no disponible");

        ErrorMessage result = handler.handleBadGatewayException(ex);

        assertEquals(502, result.getCode());
        assertNotNull(result.getMessage());
    }

    @Test
    void handleException_shouldReturn500() {
        Exception ex = new RuntimeException("Error inesperado del servidor");

        ErrorMessage result = handler.handleException(ex);

        assertEquals(500, result.getCode());
        assertNotNull(result.getMessage());
    }

    @Test
    void handleException_shouldIncludeExceptionClassName() {
        Exception ex = new IllegalStateException("Estado ilegal");

        ErrorMessage result = handler.handleException(ex);

        assertEquals("IllegalStateException", result.getError());
    }
}

