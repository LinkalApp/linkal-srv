package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.model.enums.CampaignStatus;
import es.miw.tfm.linkal.domain.persistence.CampaignPersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampaignServiceTest {
    @Mock
    private CampaignPersistence campaignPersistence;

    @InjectMocks
    private CampaignService campaignService;

    // --------------------------------------------------------------------------
    //  create
    // --------------------------------------------------------------------------

    @Test
    void create_shouldDelegateToPersistence() {
        Campaign campaign = buildCampaign();
        Campaign saved = buildSavedCampaign();

        when(campaignPersistence.create(campaign, "business@test.com")).thenReturn(saved);

        campaignService.create(campaign, "business@test.com");

        verify(campaignPersistence).create(campaign, "business@test.com");
    }

    @Test
    void create_shouldReturnCampaignFromPersistence() {
        Campaign campaign = buildCampaign();
        Campaign saved = buildSavedCampaign();

        when(campaignPersistence.create(any(), eq("business@test.com"))).thenReturn(saved);

        Campaign result = campaignService.create(campaign, "business@test.com");

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(saved.getId(), result.getId());
        assertEquals(CampaignStatus.OPEN, result.getStatus());
    }

    @Test
    void create_shouldPassEmailToPersistence() {
        Campaign campaign = buildCampaign();
        when(campaignPersistence.create(any(), any())).thenReturn(buildSavedCampaign());

        campaignService.create(campaign, "otro@empresa.com");

        verify(campaignPersistence).create(campaign, "otro@empresa.com");
    }

    @Test
    void create_shouldReturnCampaignWithCreationDate() {
        Campaign campaign = buildCampaign();
        Campaign saved = buildSavedCampaign();
        saved.setCreationDate(LocalDate.now());

        when(campaignPersistence.create(any(), any())).thenReturn(saved);

        Campaign result = campaignService.create(campaign, "business@test.com");

        assertNotNull(result.getCreationDate());
    }

    // ------------------------------------------------------------------------
    //  findByBusinessId
    // -------------------------------------------------------------------------

    @Test
    void findByBusinessId_shouldDelegateToPersistence() {
        UUID businessId = UUID.randomUUID();
        when(campaignPersistence.findByBusinessId(businessId)).thenReturn(List.of());

        campaignService.findByBusinessId(businessId);

        verify(campaignPersistence).findByBusinessId(businessId);
    }

    @Test
    void findByBusinessId_shouldReturnCampaignsFromPersistence() {
        UUID businessId = UUID.randomUUID();
        List<Campaign> campaigns = List.of(buildSavedCampaign(), buildSavedCampaign());

        when(campaignPersistence.findByBusinessId(businessId)).thenReturn(campaigns);

        List<Campaign> result = campaignService.findByBusinessId(businessId);

        assertEquals(2, result.size());
    }

    @Test
    void findByBusinessId_shouldReturnEmptyListWhenNoCampaigns() {
        UUID businessId = UUID.randomUUID();
        when(campaignPersistence.findByBusinessId(businessId)).thenReturn(List.of());

        List<Campaign> result = campaignService.findByBusinessId(businessId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByBusinessId_shouldPassCorrectId() {
        UUID businessId = UUID.randomUUID();
        when(campaignPersistence.findByBusinessId(any())).thenReturn(List.of());

        campaignService.findByBusinessId(businessId);

        verify(campaignPersistence).findByBusinessId(businessId);
        verifyNoMoreInteractions(campaignPersistence);
    }

    // --------------------------------------------------------------------------
    //  findAllOpen
    // --------------------------------------------------------------------------

    @Test
    void findAllOpen_shouldDelegateToPersistence() {
        when(campaignPersistence.findAllOpen()).thenReturn(List.of());

        campaignService.findAllOpen();

        verify(campaignPersistence).findAllOpen();
    }

    @Test
    void findAllOpen_shouldReturnAllOpenCampaigns() {
        List<Campaign> open = List.of(buildSavedCampaign(), buildSavedCampaign());
        when(campaignPersistence.findAllOpen()).thenReturn(open);

        List<Campaign> result = campaignService.findAllOpen();

        assertEquals(2, result.size());
    }

    @Test
    void findAllOpen_shouldReturnEmptyListWhenNoCampaigns() {
        when(campaignPersistence.findAllOpen()).thenReturn(List.of());

        List<Campaign> result = campaignService.findAllOpen();

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllOpen_shouldReturnCampaignsWithBusinessData() {
        Campaign c = buildSavedCampaign();
        c.setBusinessName("Mi Negocio");
        c.setBusinessCategory("Moda y Ropa");
        when(campaignPersistence.findAllOpen()).thenReturn(List.of(c));

        List<Campaign> result = campaignService.findAllOpen();

        assertEquals("Mi Negocio", result.get(0).getBusinessName());
        assertEquals("Moda y Ropa", result.get(0).getBusinessCategory());
    }

    // --------------------------------------------------------------------------
    //  findOpenByFilters
    // --------------------------------------------------------------------------

    @Test
    void findOpenByFilters_shouldDelegateToPersistence() {
        when(campaignPersistence.findOpenByFilters("Tecnología", "Madrid")).thenReturn(List.of());

        campaignService.findOpenByFilters("Tecnología", "Madrid");

        verify(campaignPersistence).findOpenByFilters("Tecnología", "Madrid");
    }

    @Test
    void findOpenByFilters_shouldReturnFilteredCampaigns() {
        Campaign c = buildSavedCampaign();
        c.setBusinessCategory("Tecnología");
        c.setBusinessProvince("Madrid");
        when(campaignPersistence.findOpenByFilters("Tecnología", "Madrid")).thenReturn(List.of(c));

        List<Campaign> result = campaignService.findOpenByFilters("Tecnología", "Madrid");

        assertEquals(1, result.size());
        assertEquals("Tecnología", result.get(0).getBusinessCategory());
        assertEquals("Madrid", result.get(0).getBusinessProvince());
    }

    @Test
    void findOpenByFilters_withOnlyCategory_shouldDelegate() {
        when(campaignPersistence.findOpenByFilters("Moda y Ropa", null)).thenReturn(List.of());

        campaignService.findOpenByFilters("Moda y Ropa", null);

        verify(campaignPersistence).findOpenByFilters("Moda y Ropa", null);
    }

    @Test
    void findOpenByFilters_withOnlyProvince_shouldDelegate() {
        when(campaignPersistence.findOpenByFilters(null, "Barcelona")).thenReturn(List.of());

        campaignService.findOpenByFilters(null, "Barcelona");

        verify(campaignPersistence).findOpenByFilters(null, "Barcelona");
    }

    @Test
    void findOpenByFilters_withOtras_shouldDelegate() {
        Campaign c = buildSavedCampaign();
        c.setBusinessCategory("Yoga y Meditación");
        when(campaignPersistence.findOpenByFilters("Otras", null)).thenReturn(List.of(c));

        List<Campaign> result = campaignService.findOpenByFilters("Otras", null);

        assertEquals(1, result.size());
        verify(campaignPersistence).findOpenByFilters("Otras", null);
    }

    @Test
    void findOpenByFilters_shouldReturnEmptyListWhenNoMatch() {
        when(campaignPersistence.findOpenByFilters("Gaming", "Sevilla")).thenReturn(List.of());

        List<Campaign> result = campaignService.findOpenByFilters("Gaming", "Sevilla");

        assertTrue(result.isEmpty());
    }

    @Test
    void findOpenByFilters_shouldNotCallFindAllOpen() {
        when(campaignPersistence.findOpenByFilters(any(), any())).thenReturn(List.of());

        campaignService.findOpenByFilters("Tecnología", "Madrid");

        verify(campaignPersistence, never()).findAllOpen();
    }

    // --------------------------------------------------------------------------
    //  update
    // --------------------------------------------------------------------------

    @Test
    void update_shouldDelegateToPersistence() {
        UUID id = UUID.randomUUID();
        Campaign campaign = buildUpdateCampaign();
        when(campaignPersistence.update(id, campaign, "business@test.com")).thenReturn(buildSavedCampaign());

        campaignService.update(id, campaign, "business@test.com");

        verify(campaignPersistence).update(id, campaign, "business@test.com");
    }

    @Test
    void update_shouldReturnUpdatedCampaign() {
        UUID id = UUID.randomUUID();
        Campaign campaign = buildUpdateCampaign();
        Campaign updated = buildSavedCampaign();
        updated.setTitle("Título actualizado");
        updated.setStatus(CampaignStatus.IN_PROGRESS);

        when(campaignPersistence.update(any(), any(), any())).thenReturn(updated);

        Campaign result = campaignService.update(id, campaign, "business@test.com");

        assertNotNull(result);
        assertEquals("Título actualizado", result.getTitle());
        assertEquals(CampaignStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    void update_shouldPassCorrectEmail() {
        UUID id = UUID.randomUUID();
        Campaign campaign = buildUpdateCampaign();
        when(campaignPersistence.update(any(), any(), any())).thenReturn(buildSavedCampaign());

        campaignService.update(id, campaign, "otro@empresa.com");

        verify(campaignPersistence).update(eq(id), eq(campaign), eq("otro@empresa.com"));
    }

    @Test
    void update_shouldNotCallCreate() {
        UUID id = UUID.randomUUID();
        Campaign campaign = buildUpdateCampaign();
        when(campaignPersistence.update(any(), any(), any())).thenReturn(buildSavedCampaign());

        campaignService.update(id, campaign, "business@test.com");

        verify(campaignPersistence, never()).create(any(), any());
    }

    // -------------------------------------------------------------------------
    //  delete
    // --------------------------------------------------------------------------

    @Test
    void delete_shouldDelegateToPersistence() {
        UUID id = UUID.randomUUID();
        doNothing().when(campaignPersistence).delete(id, "business@test.com");

        campaignService.delete(id, "business@test.com");

        verify(campaignPersistence).delete(id, "business@test.com");
    }

    @Test
    void delete_shouldPassCorrectEmail() {
        UUID id = UUID.randomUUID();

        campaignService.delete(id, "otro@empresa.com");

        verify(campaignPersistence).delete(eq(id), eq("otro@empresa.com"));
    }

    @Test
    void delete_shouldNotCallCreate() {
        UUID id = UUID.randomUUID();

        campaignService.delete(id, "business@test.com");

        verify(campaignPersistence, never()).create(any(), any());
    }

    @Test
    void delete_shouldNotCallUpdate() {
        UUID id = UUID.randomUUID();

        campaignService.delete(id, "business@test.com");

        verify(campaignPersistence, never()).update(any(), any(), any());
    }

    // --------------------------------------------------------------------------
    //  startWithInfluencer
    // ---------------------------------------------------------------------------

    @Test
    void startWithInfluencer_shouldDelegateToPersistence() {
        UUID campaignId = UUID.randomUUID();
        UUID matchId    = UUID.randomUUID();
        Campaign inProgress = buildSavedCampaign();
        inProgress.setStatus(CampaignStatus.IN_PROGRESS);

        when(campaignPersistence.startWithInfluencer(campaignId, matchId, "business@test.com"))
                .thenReturn(inProgress);

        campaignService.startWithInfluencer(campaignId, matchId, "business@test.com");

        verify(campaignPersistence).startWithInfluencer(campaignId, matchId, "business@test.com");
    }

    @Test
    void startWithInfluencer_shouldReturnInProgressCampaign() {
        UUID campaignId = UUID.randomUUID();
        UUID matchId    = UUID.randomUUID();
        Campaign inProgress = buildSavedCampaign();
        inProgress.setStatus(CampaignStatus.IN_PROGRESS);

        when(campaignPersistence.startWithInfluencer(any(), any(), any())).thenReturn(inProgress);

        Campaign result = campaignService.startWithInfluencer(campaignId, matchId, "business@test.com");

        assertNotNull(result);
        assertEquals(CampaignStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    void startWithInfluencer_shouldPassAllParamsToPersistence() {
        UUID campaignId = UUID.randomUUID();
        UUID matchId    = UUID.randomUUID();
        when(campaignPersistence.startWithInfluencer(any(), any(), any())).thenReturn(buildSavedCampaign());

        campaignService.startWithInfluencer(campaignId, matchId, "otro@empresa.com");

        verify(campaignPersistence).startWithInfluencer(eq(campaignId), eq(matchId), eq("otro@empresa.com"));
    }

    @Test
    void startWithInfluencer_shouldNotCallCreate() {
        when(campaignPersistence.startWithInfluencer(any(), any(), any())).thenReturn(buildSavedCampaign());

        campaignService.startWithInfluencer(UUID.randomUUID(), UUID.randomUUID(), "business@test.com");

        verify(campaignPersistence, never()).create(any(), any());
    }

    // ------------------------------------------------------------------------
    //  helpers
    // ------------------------------------------------------------------------

    private Campaign buildCampaign() {
        return Campaign.builder()
                .title("Campaña Verano")
                .description("Descripción de la campaña")
                .requirements("500 seguidores mínimo")
                .reward("Descuento 20%")
                .objective("Aumentar ventas")
                .build();
    }

    private Campaign buildSavedCampaign() {
        return Campaign.builder()
                .id(UUID.randomUUID())
                .title("Campaña Verano")
                .description("Descripción de la campaña")
                .requirements("500 seguidores mínimo")
                .reward("Descuento 20%")
                .objective("Aumentar ventas")
                .status(CampaignStatus.OPEN)
                .creationDate(LocalDate.now())
                .build();
    }

    private Campaign buildUpdateCampaign() {
        Campaign campaign = new Campaign();
        campaign.setTitle("Título actualizado");
        campaign.setDescription("Nueva descripción");
        campaign.setStatus(CampaignStatus.IN_PROGRESS);
        return campaign;
    }
}
