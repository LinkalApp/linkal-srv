package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ForbiddenException;
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
    //  update
    // -------------------------------------------------------------------------

    @Test
    void update_shouldThrowNotFoundWhenCampaignNotFound() {
        UUID id = UUID.randomUUID();
        when(campaignRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> campaignPersistenceJpa.update(id, new Campaign(), "owner@test.com"));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowForbiddenWhenNotOwner() {
        UUID id = UUID.randomUUID();
        CampaignEntity entity = buildCampaignEntity(buildBusinessEntity("owner@test.com"));

        when(campaignRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThrows(ForbiddenException.class,
                () -> campaignPersistenceJpa.update(id, new Campaign(), "otro@test.com"));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateTitle() {
        UUID id = UUID.randomUUID();
        CampaignEntity entity = buildCampaignEntity(buildBusinessEntity("owner@test.com"));
        when(campaignRepository.findById(id)).thenReturn(Optional.of(entity));
        when(campaignRepository.save(entity)).thenReturn(entity);

        Campaign campaign = new Campaign();
        campaign.setTitle("Nuevo título");

        campaignPersistenceJpa.update(id, campaign, "owner@test.com");

        assertEquals("Nuevo título", entity.getTitle());
    }

    @Test
    void update_shouldUpdateStatus() {
        UUID id = UUID.randomUUID();
        CampaignEntity entity = buildCampaignEntity(buildBusinessEntity("owner@test.com"));
        when(campaignRepository.findById(id)).thenReturn(Optional.of(entity));
        when(campaignRepository.save(entity)).thenReturn(entity);

        Campaign campaign = new Campaign();
        campaign.setStatus(CampaignStatus.CLOSED);

        campaignPersistenceJpa.update(id, campaign, "owner@test.com");

        assertEquals(CampaignStatus.CLOSED, entity.getStatus());
    }

    @Test
    void update_shouldUpdateAllFields() {
        UUID id = UUID.randomUUID();
        CampaignEntity entity = buildCampaignEntity(buildBusinessEntity("owner@test.com"));
        when(campaignRepository.findById(id)).thenReturn(Optional.of(entity));
        when(campaignRepository.save(entity)).thenReturn(entity);

        Campaign campaign = new Campaign();
        campaign.setTitle("Nuevo título");
        campaign.setDescription("Nueva descripción");
        campaign.setRequirements("Nuevos requisitos");
        campaign.setReward("Nueva recompensa");
        campaign.setObjective("Nuevo objetivo");
        campaign.setStatus(CampaignStatus.IN_PROGRESS);

        campaignPersistenceJpa.update(id, campaign, "owner@test.com");

        assertEquals("Nuevo título", entity.getTitle());
        assertEquals("Nueva descripción", entity.getDescription());
        assertEquals("Nuevos requisitos", entity.getRequirements());
        assertEquals("Nueva recompensa", entity.getReward());
        assertEquals("Nuevo objetivo", entity.getObjective());
        assertEquals(CampaignStatus.IN_PROGRESS, entity.getStatus());
    }

    @Test
    void update_shouldNotOverwriteFieldsWhenValueIsNull() {
        UUID id = UUID.randomUUID();
        CampaignEntity entity = buildCampaignEntity(buildBusinessEntity("owner@test.com"));
        entity.setTitle("Título original");
        entity.setReward("Recompensa original");

        when(campaignRepository.findById(id)).thenReturn(Optional.of(entity));
        when(campaignRepository.save(entity)).thenReturn(entity);

        campaignPersistenceJpa.update(id, new Campaign(), "owner@test.com");

        assertEquals("Título original", entity.getTitle());
        assertEquals("Recompensa original", entity.getReward());
    }

    @Test
    void update_shouldSaveAndReturnCampaign() {
        UUID id = UUID.randomUUID();
        CampaignEntity entity = buildCampaignEntity(buildBusinessEntity("owner@test.com"));
        when(campaignRepository.findById(id)).thenReturn(Optional.of(entity));
        when(campaignRepository.save(entity)).thenReturn(entity);

        Campaign campaign = new Campaign();
        campaign.setTitle("Nuevo título");

        Campaign result = campaignPersistenceJpa.update(id, campaign, "owner@test.com");

        assertNotNull(result);
        verify(campaignRepository).save(entity);
    }

    // --------------------------------------------------------------------------
    //  delete
    // --------------------------------------------------------------------------

    @Test
    void delete_shouldThrowNotFoundWhenCampaignNotFound() {
        UUID id = UUID.randomUUID();
        when(campaignRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> campaignPersistenceJpa.delete(id, "owner@test.com"));
        verify(campaignRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowForbiddenWhenNotOwner() {
        UUID id = UUID.randomUUID();
        CampaignEntity entity = buildCampaignEntity(buildBusinessEntity("owner@test.com"));
        when(campaignRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThrows(ForbiddenException.class,
                () -> campaignPersistenceJpa.delete(id, "otro@test.com"));
        verify(campaignRepository, never()).delete(any());
    }

    @Test
    void delete_shouldDeleteEntityWhenOwner() {
        UUID id = UUID.randomUUID();
        CampaignEntity entity = buildCampaignEntity(buildBusinessEntity("owner@test.com"));
        when(campaignRepository.findById(id)).thenReturn(Optional.of(entity));

        campaignPersistenceJpa.delete(id, "owner@test.com");

        verify(campaignRepository).delete(entity);
    }

    @Test
    void delete_shouldNotSaveAfterDelete() {
        UUID id = UUID.randomUUID();
        CampaignEntity entity = buildCampaignEntity(buildBusinessEntity("owner@test.com"));
        when(campaignRepository.findById(id)).thenReturn(Optional.of(entity));

        campaignPersistenceJpa.delete(id, "owner@test.com");

        verify(campaignRepository, never()).save(any());
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
        return buildBusinessEntity("business@test.com");
    }

    private BusinessEntity buildBusinessEntity(String email) {
        BusinessEntity entity = new BusinessEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Mi Empresa");
        entity.setEmail(email);
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
