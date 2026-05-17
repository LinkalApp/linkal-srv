package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.model.enums.CampaignStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertNotSame;

public class CampaignEntityTest {
    // -------------------------------------------------------------------------
    //  Constructor CampaignEntity(Campaign, BusinessEntity)
    // -------------------------------------------------------------------------

    @Test
    void constructor_shouldCopyTitleAndDescription() {
        Campaign campaign = buildCampaign();
        BusinessEntity business = buildBusinessEntity();

        CampaignEntity entity = new CampaignEntity(campaign, business);

        assertEquals("Campaña Verano", entity.getTitle());
        assertEquals("Descripción de la campaña", entity.getDescription());
        assertEquals("Requisitos mínimos", entity.getRequirements());
        assertEquals("Objetivo principal", entity.getObjective());
    }

    @Test
    void constructor_shouldCopyCreationDate() {
        Campaign campaign = buildCampaign();
        LocalDate date = LocalDate.of(2025, 6, 1);
        campaign.setCreationDate(date);
        BusinessEntity business = buildBusinessEntity();

        CampaignEntity entity = new CampaignEntity(campaign, business);

        assertEquals(date, entity.getCreationDate());
    }

    @Test
    void constructor_shouldCopyStatus() {
        Campaign campaign = buildCampaign();
        campaign.setStatus(CampaignStatus.CLOSED);
        BusinessEntity business = buildBusinessEntity();

        CampaignEntity entity = new CampaignEntity(campaign, business);

        assertEquals(CampaignStatus.CLOSED, entity.getStatus());
    }

    @Test
    void constructor_shouldSetBusinessReference() {
        Campaign campaign = buildCampaign();
        BusinessEntity business = buildBusinessEntity();

        CampaignEntity entity = new CampaignEntity(campaign, business);

        assertSame(business, entity.getBusiness());
        assertEquals(business.getId(), entity.getBusiness().getId());
    }

    @Test
    void constructor_shouldHandleNullBusiness() {
        Campaign campaign = buildCampaign();

        CampaignEntity entity = new CampaignEntity(campaign, null);

        assertNull(entity.getBusiness());
    }

    // -------------------------------------------------------------------------
    //  toCampaign()
    // -------------------------------------------------------------------------

    @Test
    void toCampaign_shouldMapTitleAndDescription() {
        CampaignEntity entity = buildCampaignEntity();

        Campaign campaign = entity.toCampaign();

        assertEquals("Campaña Verano", campaign.getTitle());
        assertEquals("Descripción de la campaña", campaign.getDescription());
        assertEquals("Requisitos mínimos", campaign.getRequirements());
        assertEquals("Objetivo principal", campaign.getObjective());
    }

    @Test
    void toCampaign_shouldMapId() {
        CampaignEntity entity = buildCampaignEntity();
        UUID id = UUID.randomUUID();
        entity.setId(id);

        Campaign campaign = entity.toCampaign();

        assertEquals(id, campaign.getId());
    }

    @Test
    void toCampaign_shouldMapStatus() {
        CampaignEntity entity = buildCampaignEntity();
        entity.setStatus(CampaignStatus.OPEN);

        Campaign campaign = entity.toCampaign();

        assertEquals(CampaignStatus.OPEN, campaign.getStatus());
    }

    @Test
    void toCampaign_shouldSetBusinessId_whenBusinessIsNotNull() {
        CampaignEntity entity = buildCampaignEntity();
        BusinessEntity business = buildBusinessEntity();
        entity.setBusiness(business);

        Campaign campaign = entity.toCampaign();

        assertEquals(business.getId(), campaign.getBusinessId());
    }

    @Test
    void toCampaign_shouldLeaveBusinessIdNull_whenBusinessIsNull() {
        CampaignEntity entity = buildCampaignEntity();
        entity.setBusiness(null);

        Campaign campaign = entity.toCampaign();

        assertNull(campaign.getBusinessId());
    }

    @Test
    void toCampaign_shouldReturnNewInstanceEachTime() {
        CampaignEntity entity = buildCampaignEntity();

        Campaign c1 = entity.toCampaign();
        Campaign c2 = entity.toCampaign();

        assertNotSame(c1, c2);
    }

    // -------------------------------------------------------------------------
    //  Helpers
    // -------------------------------------------------------------------------

    private Campaign buildCampaign() {
        Campaign campaign = new Campaign();
        campaign.setTitle("Campaña Verano");
        campaign.setDescription("Descripción de la campaña");
        campaign.setRequirements("Requisitos mínimos");
        campaign.setObjective("Objetivo principal");
        campaign.setCreationDate(LocalDate.of(2025, 6, 1));
        campaign.setStatus(CampaignStatus.OPEN);
        return campaign;
    }

    private BusinessEntity buildBusinessEntity() {
        BusinessEntity entity = new BusinessEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Mi Empresa");
        entity.setEmail("empresa@test.com");
        entity.setPassword("hashedPass");
        return entity;
    }

    private CampaignEntity buildCampaignEntity() {
        CampaignEntity entity = new CampaignEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle("Campaña Verano");
        entity.setDescription("Descripción de la campaña");
        entity.setRequirements("Requisitos mínimos");
        entity.setObjective("Objetivo principal");
        entity.setCreationDate(LocalDate.of(2025, 6, 1));
        entity.setStatus(CampaignStatus.OPEN);
        return entity;
    }
}
