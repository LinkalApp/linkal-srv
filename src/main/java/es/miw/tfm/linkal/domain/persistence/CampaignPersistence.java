package es.miw.tfm.linkal.domain.persistence;

import es.miw.tfm.linkal.domain.model.Campaign;

import java.util.List;
import java.util.UUID;

public interface CampaignPersistence {
    Campaign create(Campaign campaign, String businessEmail);
    List<Campaign> findByBusinessId(UUID businessId);
    List<Campaign> findAllOpen();
    Campaign update(UUID id, Campaign campaign, String businessEmail);
    void delete(UUID id, String businessEmail);
}
