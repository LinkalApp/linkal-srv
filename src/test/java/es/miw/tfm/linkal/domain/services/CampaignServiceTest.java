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
}
