package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.persistence.CampaignPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignPersistence campaignPersistence;

    public Campaign create(Campaign campaign, String businessEmail) {
        return campaignPersistence.create(campaign, businessEmail);
    }

    public List<Campaign> findByBusinessId(UUID businessId) {
        return campaignPersistence.findByBusinessId(businessId);
    }
    public List<Campaign> findAllOpen() { return campaignPersistence.findAllOpen(); }
    public List<Campaign> findOpenByFilters(String category, String province) {
        return campaignPersistence.findOpenByFilters(category, province);
    }

    public Campaign update(UUID id, Campaign campaign, String businessEmail) {
        return campaignPersistence.update(id, campaign, businessEmail);
    }

    public void delete(UUID id, String businessEmail) {
        campaignPersistence.delete(id, businessEmail);
    }
}
