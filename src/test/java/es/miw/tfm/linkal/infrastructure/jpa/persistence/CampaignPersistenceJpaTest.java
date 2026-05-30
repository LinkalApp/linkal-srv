package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.model.enums.CampaignStatus;
import es.miw.tfm.linkal.infrastructure.jpa.entities.BusinessEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.CampaignEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.BusinessRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.CampaignRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampaignPersistenceJpaTest {
    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private CampaignPersistenceJpa campaignPersistenceJpa;

    // -------------------------------------------------------------------------
    //  create
    // --------------------------------------------------------------------------

    @Test
    void create_shouldThrowNotFoundExceptionWhenBusinessNotFound() {
        Campaign campaign = buildCampaign();

        when(businessRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> campaignPersistenceJpa.create(campaign, "unknown@test.com"));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void create_shouldSaveEntityAndReturnCampaign() {
        Campaign campaign = buildCampaign();
        BusinessEntity business = buildBusinessEntity();
        CampaignEntity savedEntity = buildCampaignEntity(business);

        when(businessRepository.findByEmail("business@test.com")).thenReturn(Optional.of(business));
        when(campaignRepository.save(any(CampaignEntity.class))).thenReturn(savedEntity);

        Campaign result = campaignPersistenceJpa.create(campaign, "business@test.com");

        assertNotNull(result);
        assertEquals("Campaña Verano", result.getTitle());
        verify(campaignRepository).save(any(CampaignEntity.class));
    }

    @Test
    void create_shouldSetCreationDateToToday() {
        Campaign campaign = buildCampaign();
        BusinessEntity business = buildBusinessEntity();
        CampaignEntity savedEntity = buildCampaignEntity(business);

        when(businessRepository.findByEmail("business@test.com")).thenReturn(Optional.of(business));
        when(campaignRepository.save(any(CampaignEntity.class))).thenReturn(savedEntity);

        campaignPersistenceJpa.create(campaign, "business@test.com");

        assertEquals(LocalDate.now(), campaign.getCreationDate());
    }

    @Test
    void create_shouldSetStatusToOpen() {
        Campaign campaign = buildCampaign();
        BusinessEntity business = buildBusinessEntity();
        CampaignEntity savedEntity = buildCampaignEntity(business);

        when(businessRepository.findByEmail("business@test.com")).thenReturn(Optional.of(business));
        when(campaignRepository.save(any(CampaignEntity.class))).thenReturn(savedEntity);

        campaignPersistenceJpa.create(campaign, "business@test.com");

        assertEquals(CampaignStatus.OPEN, campaign.getStatus());
    }

    @Test
    void create_shouldLookUpBusinessByEmail() {
        Campaign campaign = buildCampaign();
        BusinessEntity business = buildBusinessEntity();
        CampaignEntity savedEntity = buildCampaignEntity(business);

        when(businessRepository.findByEmail("business@test.com")).thenReturn(Optional.of(business));
        when(campaignRepository.save(any())).thenReturn(savedEntity);

        campaignPersistenceJpa.create(campaign, "business@test.com");

        verify(businessRepository).findByEmail("business@test.com");
    }

    @Test
    void create_shouldSaveEntityWithCorrectFields() {
        Campaign campaign = buildCampaign();
        BusinessEntity business = buildBusinessEntity();
        CampaignEntity savedEntity = buildCampaignEntity(business);

        when(businessRepository.findByEmail("business@test.com")).thenReturn(Optional.of(business));
        when(campaignRepository.save(any(CampaignEntity.class))).thenReturn(savedEntity);

        ArgumentCaptor<CampaignEntity> captor = ArgumentCaptor.forClass(CampaignEntity.class);

        campaignPersistenceJpa.create(campaign, "business@test.com");

        verify(campaignRepository).save(captor.capture());
        CampaignEntity captured = captor.getValue();
        assertEquals("Campaña Verano", captured.getTitle());
        assertSame(business, captured.getBusiness());
    }

    @Test
    void create_shouldReturnCampaignWithBusinessId() {
        Campaign campaign = buildCampaign();
        BusinessEntity business = buildBusinessEntity();
        CampaignEntity savedEntity = buildCampaignEntity(business);

        when(businessRepository.findByEmail("business@test.com")).thenReturn(Optional.of(business));
        when(campaignRepository.save(any(CampaignEntity.class))).thenReturn(savedEntity);

        Campaign result = campaignPersistenceJpa.create(campaign, "business@test.com");

        assertEquals(business.getId(), result.getBusinessId());
    }

    // --------------------------------------------------------------------------
    //  findByBusinessId
    // --------------------------------------------------------------------------

    @Test
    void findByBusinessId_shouldReturnMappedCampaigns() {
        UUID businessId = UUID.randomUUID();
        BusinessEntity business = buildBusinessEntity();
        business.setId(businessId);

        CampaignEntity e1 = buildCampaignEntity(business);
        CampaignEntity e2 = buildCampaignEntity(business);

        when(campaignRepository.findAllByBusinessId(businessId)).thenReturn(List.of(e1, e2));

        List<Campaign> result = campaignPersistenceJpa.findByBusinessId(businessId);

        assertEquals(2, result.size());
        verify(campaignRepository).findAllByBusinessId(businessId);
    }

    @Test
    void findByBusinessId_shouldReturnEmptyListWhenNoCampaigns() {
        UUID businessId = UUID.randomUUID();

        when(campaignRepository.findAllByBusinessId(businessId)).thenReturn(List.of());

        List<Campaign> result = campaignPersistenceJpa.findByBusinessId(businessId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByBusinessId_shouldMapTitleCorrectly() {
        UUID businessId = UUID.randomUUID();
        BusinessEntity business = buildBusinessEntity();
        business.setId(businessId);
        CampaignEntity entity = buildCampaignEntity(business);

        when(campaignRepository.findAllByBusinessId(businessId)).thenReturn(List.of(entity));

        List<Campaign> result = campaignPersistenceJpa.findByBusinessId(businessId);

        assertEquals("Campaña Verano", result.get(0).getTitle());
        assertEquals(businessId, result.get(0).getBusinessId());
    }

    // ------------------------------------------------------------------------
    //  helpers
    // ------------------------------------------------------------------------

    private Campaign buildCampaign() {
        Campaign campaign = new Campaign();
        campaign.setTitle("Campaña Verano");
        campaign.setDescription("Descripción de prueba");
        campaign.setRequirements("500 seguidores mínimo");
        campaign.setReward("Descuento 20%");
        campaign.setObjective("Aumentar ventas");
        return campaign;
    }

    private BusinessEntity buildBusinessEntity() {
        BusinessEntity entity = new BusinessEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Mi Empresa");
        entity.setEmail("business@test.com");
        entity.setPassword("hashedPass");
        return entity;
    }

    private CampaignEntity buildCampaignEntity(BusinessEntity business) {
        CampaignEntity entity = new CampaignEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle("Campaña Verano");
        entity.setDescription("Descripción de prueba");
        entity.setRequirements("500 seguidores mínimo");
        entity.setReward("Descuento 20%");
        entity.setObjective("Aumentar ventas");
        entity.setCreationDate(LocalDate.now());
        entity.setStatus(CampaignStatus.OPEN);
        entity.setBusiness(business);
        entity.setMatches(new ArrayList<>());
        entity.setChats(new ArrayList<>());
        return entity;
    }
}
