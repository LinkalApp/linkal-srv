package es.miw.tfm.linkal.domain.persistence;

import es.miw.tfm.linkal.domain.model.Campaign;

public interface CampaignPersistence {
    Campaign create(Campaign campaign, String businessEmail);
}
