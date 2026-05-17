package es.miw.tfm.linkal.domain.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    @Test
    void badRequestException_shouldContainMessage() {
        BadRequestException ex = new BadRequestException("detalle del error");

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("detalle del error"));
    }

    @Test
    void notFoundException_shouldContainMessage() {
        NotFoundException ex = new NotFoundException("recurso no encontrado");

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("recurso no encontrado"));
    }

    @Test
    void conflictException_shouldContainMessage() {
        ConflictException ex = new ConflictException("email duplicado");

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("email duplicado"));
    }

    @Test
    void forbiddenException_shouldContainMessage() {
        ForbiddenException ex = new ForbiddenException("sin permisos");

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("sin permisos"));
    }

    @Test
    void badGatewayException_shouldContainMessage() {
        BadGatewayException ex = new BadGatewayException("servicio externo caído");

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("servicio externo caído"));
    }

    @Test
    void allExceptions_shouldExtendRuntimeException() {
        assertInstanceOf(RuntimeException.class, new BadRequestException("x"));
        assertInstanceOf(RuntimeException.class, new NotFoundException("x"));
        assertInstanceOf(RuntimeException.class, new ConflictException("x"));
        assertInstanceOf(RuntimeException.class, new ForbiddenException("x"));
        assertInstanceOf(RuntimeException.class, new BadGatewayException("x"));
    }
}

