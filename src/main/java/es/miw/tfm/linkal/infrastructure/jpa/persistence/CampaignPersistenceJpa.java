package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ForbiddenException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Business;
import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.model.enums.BusinessCategory;
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
import java.util.List;
import java.util.UUID;

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

    @Override
    public List<Campaign> findByBusinessId(UUID businessId) {
        return campaignRepository.findAllByBusinessId(businessId).stream()
                .map(CampaignEntity::toCampaign)
                .toList();
    }

    @Override
    @Transactional
    public Campaign update(UUID id, Campaign campaign, String businessEmail) {
        CampaignEntity entity = campaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found: " + id));

        if (!entity.getBusiness().getEmail().equals(businessEmail)) {
            throw new ForbiddenException("No tienes permiso para editar esta campaña");
        }

        applyUpdates(entity, campaign);

        return campaignRepository.save(entity).toCampaign();
    }

    private void applyUpdates(CampaignEntity entity, Campaign campaign) {
        if (campaign.getTitle()        != null) entity.setTitle(campaign.getTitle());
        if (campaign.getDescription()  != null) entity.setDescription(campaign.getDescription());
        if (campaign.getRequirements() != null) entity.setRequirements(campaign.getRequirements());
        if (campaign.getReward()       != null) entity.setReward(campaign.getReward());
        if (campaign.getObjective()    != null) entity.setObjective(campaign.getObjective());
        if (campaign.getStatus()       != null) entity.setStatus(campaign.getStatus());
    }

    @Override
    @Transactional
    public void delete(UUID id, String businessEmail) {
        CampaignEntity entity = campaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found: " + id));

        if (!entity.getBusiness().getEmail().equals(businessEmail)) {
            throw new ForbiddenException("No tienes permiso para eliminar esta campaña");
        }

        campaignRepository.delete(entity);
    }

    @Override
    public List<Campaign> findAllOpen() {
        return enrich(campaignRepository.findAllByStatus(CampaignStatus.OPEN));
    }

    @Override
    public List<Campaign> findOpenByFilters(String category, String province) {
        String cat  = (category != null && !category.isBlank()) ? category : null;
        String prov = (province != null && !province.isBlank()) ? province : null;

        if (BusinessCategory.OTHER.equals(cat)) {
            return enrich(campaignRepository.findOpenByOtherCategories(BusinessCategory.STANDARD, prov));
        }
        return enrich(campaignRepository.findOpenByFilters(cat, prov));
    }

    private List<Campaign> enrich(List<CampaignEntity> entities) {
        return entities.stream()
                .map(entity -> {
                    Campaign campaign = entity.toCampaign();
                    BusinessEntity business = entity.getBusiness();
                    if (business != null) {
                        campaign.setBusinessName(business.getName());
                        campaign.setBusinessCategory(business.getCategory());
                        campaign.setBusinessDescription(business.getDescription());
                        campaign.setBusinessWebsite(business.getWebsite());
                        campaign.setBusinessProvince(business.getProvince());
                        campaign.setBusinessAddress(business.getAddress());
                        campaign.setBusinessVerified(business.getVerified());
                    }
                    return campaign;
                })
                .toList();
    }
}
