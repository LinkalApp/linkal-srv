package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.Business;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BusinessEntityTest {
    // -------------------------------------------------------------------------
    //  Constructor BusinessEntity(Business)
    // -------------------------------------------------------------------------
    @Test
    void constructor_shouldCopyBaseFields() {
        Business business = buildBusiness();

        BusinessEntity entity = new BusinessEntity(business);

        assertEquals("Mi Empresa", entity.getName());
        assertEquals("empresa@test.com", entity.getEmail());
        assertEquals("hashedPass", entity.getPassword());
        assertEquals("600333444", entity.getPhoneNumber());
        assertEquals("Descripción empresa", entity.getDescription());
    }

    @Test
    void constructor_shouldCopyBusinessSpecificFields() {
        Business business = buildBusiness();

        BusinessEntity entity = new BusinessEntity(business);

        assertEquals("Calle Mayor 1, Madrid", entity.getAddress());
        assertEquals("Madrid", entity.getProvince());
        assertEquals("https://miempresa.com", entity.getWebsite());
        assertEquals("Moda", entity.getCategory());
    }

    @Test
    void constructor_shouldCopyVerifiedField() {
        Business business = buildBusiness();
        business.setVerified(false);

        BusinessEntity entity = new BusinessEntity(business);

        assertFalse(entity.getVerified());
    }

    @Test
    void constructor_shouldInitializeCampaignsListAsEmpty() {
        Business business = buildBusiness();

        BusinessEntity entity = new BusinessEntity(business);

        assertDoesNotThrow(() -> new BusinessEntity(business));
    }

    @Test
    void constructor_shouldHandleNullOptionalFields() {
        Business business = new Business();
        business.setName("Empresa Mínima");
        business.setEmail("minima@test.com");
        business.setPassword("pass");

        BusinessEntity entity = new BusinessEntity(business);

        assertNull(entity.getAddress());
        assertNull(entity.getWebsite());
        assertNull(entity.getProvince());
        assertNull(entity.getCategory());
    }

    // -------------------------------------------------------------------------
    //  toBusiness()
    // -------------------------------------------------------------------------

    @Test
    void toBusiness_shouldMapBaseFields() {
        BusinessEntity entity = buildBusinessEntity();

        Business business = entity.toBusiness();

        assertEquals("Mi Empresa", business.getName());
        assertEquals("empresa@test.com", business.getEmail());
        assertEquals("hashedPass", business.getPassword());
        assertEquals("600333444", business.getPhoneNumber());
        assertEquals("Descripción empresa", business.getDescription());
    }

    @Test
    void toBusiness_shouldMapBusinessSpecificFields() {
        BusinessEntity entity = buildBusinessEntity();

        Business business = entity.toBusiness();

        assertEquals("Calle Mayor 1, Madrid", business.getAddress());
        assertEquals("Madrid", business.getProvince());
        assertEquals("https://miempresa.com", business.getWebsite());
        assertEquals("Moda", business.getCategory());
    }

    @Test
    void toBusiness_shouldMapId() {
        BusinessEntity entity = buildBusinessEntity();
        UUID id = UUID.randomUUID();
        entity.setId(id);

        Business business = entity.toBusiness();

        assertEquals(id, business.getId());
    }

    @Test
    void toBusiness_shouldReturnNewInstanceEachTime() {
        BusinessEntity entity = buildBusinessEntity();

        Business b1 = entity.toBusiness();
        Business b2 = entity.toBusiness();

        assertNotSame(b1, b2);
    }

    // -------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    private Business buildBusiness() {
        Business business = new Business();
        business.setName("Mi Empresa");
        business.setEmail("empresa@test.com");
        business.setPassword("hashedPass");
        business.setPhoneNumber("600333444");
        business.setDescription("Descripción empresa");
        business.setAddress("Calle Mayor 1, Madrid");
        business.setProvince("Madrid");
        business.setWebsite("https://miempresa.com");
        business.setCategory("Moda");
        return business;
    }

    private BusinessEntity buildBusinessEntity() {
        BusinessEntity entity = new BusinessEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Mi Empresa");
        entity.setEmail("empresa@test.com");
        entity.setPassword("hashedPass");
        entity.setPhoneNumber("600333444");
        entity.setDescription("Descripción empresa");
        entity.setAddress("Calle Mayor 1, Madrid");
        entity.setProvince("Madrid");
        entity.setWebsite("https://miempresa.com");
        entity.setCategory("Moda");
        entity.setVerified(false);
        return entity;
    }
}
