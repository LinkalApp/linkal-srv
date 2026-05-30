package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.model.enums.CampaignStatus;
import es.miw.tfm.linkal.domain.persistence.CampaignPersistence;
import es.miw.tfm.linkal.infrastructure.jpa.entities.BusinessEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.CampaignEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.BusinessRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class CampaignPersistenceJpa implements CampaignPersistence {
    private final CampaignRepository campaignRepository;
    private final BusinessRepository businessRepository;

    @Override
    @Transactional
    public Campaign create(Campaign campaign, String businessEmail) {
        BusinessEntity business = businessRepository.findByEmail(businessEmail)
                .orElseThrow(() -> new NotFoundException("Business not found: " + businessEmail));
        campaign.setCreationDate(LocalDate.now());
        campaign.setStatus(CampaignStatus.OPEN);
        CampaignEntity entity = new CampaignEntity(campaign, business);
        entity.setMatches(new java.util.ArrayList<>());
        entity.setChats(new java.util.ArrayList<>());
        return campaignRepository.save(entity).toCampaign();
    }
}
