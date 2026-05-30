package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.persistence.CampaignPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignPersistence campaignPersistence;

    public Campaign create(Campaign campaign, String businessEmail) {
        return campaignPersistence.create(campaign, businessEmail);
    }

}
