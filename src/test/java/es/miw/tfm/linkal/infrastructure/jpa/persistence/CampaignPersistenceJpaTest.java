package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.exceptions.ForbiddenException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.model.enums.CampaignStatus;
import es.miw.tfm.linkal.infrastructure.jpa.entities.BusinessEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.CampaignEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.MatchEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.BusinessRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.CampaignRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.EvaluationRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.MatchRepository;
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
    private MatchRepository matchRepository;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private EvaluationRepository evaluationRepository;

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
    //  findAllOpen
    // ------------------------------------------------------------------------

    @Test
    void findAllOpen_shouldReturnEmptyListWhenNoCampaigns() {
        when(campaignRepository.findAllByStatus(CampaignStatus.OPEN)).thenReturn(List.of());

        List<Campaign> result = campaignPersistenceJpa.findAllOpen();

        assertTrue(result.isEmpty());
        verify(campaignRepository).findAllByStatus(CampaignStatus.OPEN);
    }

    @Test
    void findAllOpen_shouldReturnMappedCampaigns() {
        BusinessEntity business = buildBusinessEntity();
        CampaignEntity e1 = buildCampaignEntity(business);
        CampaignEntity e2 = buildCampaignEntity(business);

        when(campaignRepository.findAllByStatus(CampaignStatus.OPEN)).thenReturn(List.of(e1, e2));

        List<Campaign> result = campaignPersistenceJpa.findAllOpen();

        assertEquals(2, result.size());
    }

    @Test
    void findAllOpen_shouldEnrichWithBusinessName() {
        BusinessEntity business = buildBusinessEntity();
        business.setName("Mi Negocio");
        CampaignEntity entity = buildCampaignEntity(business);

        when(campaignRepository.findAllByStatus(CampaignStatus.OPEN)).thenReturn(List.of(entity));

        List<Campaign> result = campaignPersistenceJpa.findAllOpen();

        assertEquals("Mi Negocio", result.get(0).getBusinessName());
    }

    @Test
    void findAllOpen_shouldEnrichWithBusinessCategory() {
        BusinessEntity business = buildBusinessEntity();
        business.setCategory("Tecnología");
        CampaignEntity entity = buildCampaignEntity(business);

        when(campaignRepository.findAllByStatus(CampaignStatus.OPEN)).thenReturn(List.of(entity));

        List<Campaign> result = campaignPersistenceJpa.findAllOpen();

        assertEquals("Tecnología", result.get(0).getBusinessCategory());
    }

    @Test
    void findAllOpen_shouldOnlyReturnOpenStatus() {
        when(campaignRepository.findAllByStatus(CampaignStatus.OPEN)).thenReturn(List.of());

        campaignPersistenceJpa.findAllOpen();

        verify(campaignRepository).findAllByStatus(CampaignStatus.OPEN);
        verify(campaignRepository, never()).findAllByBusinessId(any());
    }

    // -------------------------------------------------------------------------
    //  findOpenByFilters
    // --------------------------------------------------------------------------

    @Test
    void findOpenByFilters_withCategoryAndProvince_shouldCallRepository() {
        when(campaignRepository.findOpenByFilters("Tecnología", "Madrid")).thenReturn(List.of());

        campaignPersistenceJpa.findOpenByFilters("Tecnología", "Madrid");

        verify(campaignRepository).findOpenByFilters("Tecnología", "Madrid");
    }

    @Test
    void findOpenByFilters_withBlankCategory_shouldPassNullToRepository() {
        when(campaignRepository.findOpenByFilters(null, "Madrid")).thenReturn(List.of());

        campaignPersistenceJpa.findOpenByFilters("", "Madrid");

        verify(campaignRepository).findOpenByFilters(null, "Madrid");
    }

    @Test
    void findOpenByFilters_withBlankProvince_shouldPassNullToRepository() {
        when(campaignRepository.findOpenByFilters("Tecnología", null)).thenReturn(List.of());

        campaignPersistenceJpa.findOpenByFilters("Tecnología", "");

        verify(campaignRepository).findOpenByFilters("Tecnología", null);
    }

    @Test
    void findOpenByFilters_shouldEnrichWithBusinessData() {
        BusinessEntity business = buildBusinessEntity();
        business.setCategory("Tecnología");
        business.setProvince("Madrid");
        CampaignEntity entity = buildCampaignEntity(business);

        when(campaignRepository.findOpenByFilters("Tecnología", "Madrid")).thenReturn(List.of(entity));

        List<Campaign> result = campaignPersistenceJpa.findOpenByFilters("Tecnología", "Madrid");

        assertEquals(1, result.size());
        assertEquals("Tecnología", result.get(0).getBusinessCategory());
        assertEquals("Madrid", result.get(0).getBusinessProvince());
    }

    @Test
    void findOpenByFilters_withOtras_shouldCallFindOpenByOtherCategories() {
        when(campaignRepository.findOpenByOtherCategories(any(), eq(null))).thenReturn(List.of());

        campaignPersistenceJpa.findOpenByFilters("Otra", null);

        verify(campaignRepository).findOpenByOtherCategories(any(), eq(null));
        verify(campaignRepository, never()).findOpenByFilters(any(), any());
    }

    @Test
    void findOpenByFilters_withOtrasAndProvince_shouldPassProvinceToOtherQuery() {
        BusinessEntity business = buildBusinessEntity();
        business.setCategory("Yoga y Meditación");
        business.setProvince("Barcelona");
        CampaignEntity entity = buildCampaignEntity(business);

        when(campaignRepository.findOpenByOtherCategories(any(), eq("Barcelona"))).thenReturn(List.of(entity));

        List<Campaign> result = campaignPersistenceJpa.findOpenByFilters("Otra", "Barcelona");

        assertEquals(1, result.size());
        assertEquals("Yoga y Meditación", result.get(0).getBusinessCategory());
        verify(campaignRepository).findOpenByOtherCategories(any(), eq("Barcelona"));
    }

    @Test
    void findOpenByFilters_withOtras_shouldNotIncludeStandardCategories() {
        BusinessEntity business = buildBusinessEntity();
        business.setCategory("Yoga y Meditación");
        CampaignEntity entity = buildCampaignEntity(business);

        when(campaignRepository.findOpenByOtherCategories(any(), any())).thenReturn(List.of(entity));

        List<Campaign> result = campaignPersistenceJpa.findOpenByFilters("Otra", null);

        assertEquals("Yoga y Meditación", result.get(0).getBusinessCategory());
    }

    @Test
    void findOpenByFilters_shouldReturnEmptyListWhenNoMatch() {
        when(campaignRepository.findOpenByFilters("Gaming", "Sevilla")).thenReturn(List.of());

        List<Campaign> result = campaignPersistenceJpa.findOpenByFilters("Gaming", "Sevilla");

        assertTrue(result.isEmpty());
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

    // --------------------------------------------------------------------------
    //  startWithInfluencer
    // ---------------------------------------------------------------------------

    @Test
    void startWithInfluencer_shouldThrowNotFoundWhenCampaignDoesNotExist() {
        UUID campaignId = UUID.randomUUID();
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> campaignPersistenceJpa.startWithInfluencer(campaignId, UUID.randomUUID(), "owner@test.com"));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void startWithInfluencer_shouldThrowForbiddenWhenNotOwner() {
        BusinessEntity owner = buildBusinessEntity("owner@test.com");
        CampaignEntity campaign = buildCampaignEntity(owner);
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThrows(ForbiddenException.class,
                () -> campaignPersistenceJpa.startWithInfluencer(campaign.getId(), UUID.randomUUID(), "other@test.com"));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void startWithInfluencer_shouldThrowConflictWhenCampaignNotOpen() {
        BusinessEntity owner = buildBusinessEntity("owner@test.com");
        CampaignEntity campaign = buildCampaignEntity(owner);
        campaign.setStatus(CampaignStatus.IN_PROGRESS);
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThrows(ConflictException.class,
                () -> campaignPersistenceJpa.startWithInfluencer(campaign.getId(), UUID.randomUUID(), "owner@test.com"));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void startWithInfluencer_shouldThrowNotFoundWhenMatchDoesNotExist() {
        BusinessEntity owner = buildBusinessEntity("owner@test.com");
        CampaignEntity campaign = buildCampaignEntity(owner);
        UUID matchId = UUID.randomUUID();
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> campaignPersistenceJpa.startWithInfluencer(campaign.getId(), matchId, "owner@test.com"));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void startWithInfluencer_shouldThrowForbiddenWhenMatchNotBelongToCampaign() {
        BusinessEntity owner = buildBusinessEntity("owner@test.com");
        CampaignEntity campaign = buildCampaignEntity(owner);
        CampaignEntity otherCamp = buildCampaignEntity(owner);
        MatchEntity match = buildMatchEntity(otherCamp);

        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

        assertThrows(ForbiddenException.class,
                () -> campaignPersistenceJpa.startWithInfluencer(campaign.getId(), match.getId(), "owner@test.com"));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void startWithInfluencer_shouldDeleteOtherMatchesAndSaveCampaign() {
        BusinessEntity owner = buildBusinessEntity("owner@test.com");
        CampaignEntity campaign = buildCampaignEntity(owner);

        MatchEntity selected = buildMatchEntity(campaign);
        MatchEntity other1 = buildMatchEntity(campaign);
        MatchEntity other2 = buildMatchEntity(campaign);

        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(matchRepository.findById(selected.getId())).thenReturn(Optional.of(selected));
        when(matchRepository.findAllByCampaign_Id(campaign.getId()))
                .thenReturn(List.of(selected, other1, other2));
        when(campaignRepository.save(campaign)).thenReturn(campaign);

        campaignPersistenceJpa.startWithInfluencer(campaign.getId(), selected.getId(), "owner@test.com");

        verify(matchRepository).delete(other1);
        verify(matchRepository).delete(other2);
        verify(matchRepository, never()).delete(selected);
        verify(campaignRepository).save(campaign);
        assertEquals(CampaignStatus.IN_PROGRESS, campaign.getStatus());
    }

    @Test
    void startWithInfluencer_shouldReturnCampaignWithInProgressStatus() {
        BusinessEntity owner = buildBusinessEntity("owner@test.com");
        CampaignEntity campaign = buildCampaignEntity(owner);
        MatchEntity selected = buildMatchEntity(campaign);

        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(matchRepository.findById(selected.getId())).thenReturn(Optional.of(selected));
        when(matchRepository.findAllByCampaign_Id(campaign.getId())).thenReturn(List.of(selected));
        when(campaignRepository.save(any())).thenReturn(campaign);

        Campaign result = campaignPersistenceJpa.startWithInfluencer(campaign.getId(), selected.getId(), "owner@test.com");

        assertNotNull(result);
        assertEquals(CampaignStatus.IN_PROGRESS, campaign.getStatus());
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

    private MatchEntity buildMatchEntity(CampaignEntity campaign) {
        MatchEntity match = new MatchEntity();
        match.setId(UUID.randomUUID());
        match.setCampaign(campaign);
        return match;
    }
}
