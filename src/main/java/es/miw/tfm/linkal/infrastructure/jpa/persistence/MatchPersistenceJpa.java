package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.exceptions.ForbiddenException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Match;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import es.miw.tfm.linkal.domain.persistence.MatchPersistence;
import es.miw.tfm.linkal.infrastructure.jpa.entities.BusinessEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.CampaignEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.InfluencerEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.MatchEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.BusinessRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.CampaignRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.InfluencerRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MatchPersistenceJpa implements MatchPersistence {

    private final MatchRepository matchRepository;
    private final CampaignRepository campaignRepository;
    private final InfluencerRepository influencerRepository;
    private final BusinessRepository businessRepository;

    @Override
    @Transactional
    public Match createByInfluencer(UUID campaignId, String influencerEmail) {
        CampaignEntity campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campaign not found: " + campaignId));

        InfluencerEntity influencer = influencerRepository.findByEmail(influencerEmail)
                .orElseThrow(() -> new NotFoundException("Influencer not found: " + influencerEmail));

        // El influencer ya expresó interés antes
        Optional<MatchEntity> matchByInfluencer = matchRepository
                .findByCampaign_IdAndInfluencer_Id(campaignId, influencer.getId());

        if (matchByInfluencer.isPresent()) {
            MatchEntity existing = matchByInfluencer.get();
            if (existing.getStatus() == MatchStatus.COMPLETED) {
                throw new ConflictException("Ya existe un match completado para esta campaña");
            }
            if (existing.getBusinessId() == null) {
                // businessId null → lo inició el influencer → duplicado
                throw new ConflictException("Ya has expresado interés en esta campaña");
            }
            // businessId != null → lo inició el comercio → match mutuo
            existing.setStatus(MatchStatus.COMPLETED);
            existing.setMatchedAt(LocalDateTime.now());
            return matchRepository.save(existing).toMatch();
        }

        // Sin match previo
        MatchEntity newMatch = MatchEntity.builder()
                .campaign(campaign)
                .influencer(influencer)
                .businessId(null)
                .status(MatchStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return matchRepository.save(newMatch).toMatch();
    }

    @Override
    public Optional<Match> findByInfluencer(UUID campaignId, String influencerEmail) {
        InfluencerEntity influencer = influencerRepository.findByEmail(influencerEmail)
                .orElseThrow(() -> new NotFoundException("Influencer not found: " + influencerEmail));

        return matchRepository
                .findByCampaign_IdAndInfluencer_Id(campaignId, influencer.getId())
                .map(MatchEntity::toMatch);
    }

    @Override
    @Transactional
    public Match createByBusiness(UUID influencerId, UUID campaignId, String businessEmail) {
        BusinessEntity business = businessRepository.findByEmail(businessEmail)
                .orElseThrow(() -> new NotFoundException("Business not found: " + businessEmail));

        CampaignEntity campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campaign not found: " + campaignId));

        if (!campaign.getBusiness().getId().equals(business.getId())) {
            throw new ForbiddenException("No tienes permiso para crear un match con esta campaña");
        }

        InfluencerEntity influencer = influencerRepository.findById(influencerId)
                .orElseThrow(() -> new NotFoundException("Influencer not found: " + influencerId));

        Optional<MatchEntity> existing = matchRepository
                .findByCampaign_IdAndInfluencer_Id(campaignId, influencerId);

        if (existing.isPresent()) {
            MatchEntity match = existing.get();
            if (match.getStatus() == MatchStatus.COMPLETED) {
                throw new ConflictException("Ya existe un match completado para esta campaña");
            }
            if (match.getBusinessId() != null) {
                // businessId != null → lo inició el comercio → duplicado
                throw new ConflictException("Ya has propuesto una colaboración a este influencer para esta campaña");
            }
            // businessId null → lo inició el influencer → match mutuo
            match.setBusinessId(business.getId());
            match.setStatus(MatchStatus.COMPLETED);
            match.setMatchedAt(LocalDateTime.now());
            return matchRepository.save(match).toMatch();
        }

        return matchRepository.save(MatchEntity.builder()
                .campaign(campaign)
                .influencer(influencer)
                .businessId(business.getId())
                .status(MatchStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build()).toMatch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Match> findPendingByInfluencer(String influencerEmail) {
        InfluencerEntity influencer = influencerRepository.findByEmail(influencerEmail)
                .orElseThrow(() -> new NotFoundException("Influencer not found: " + influencerEmail));

        return matchRepository
                .findByInfluencer_IdAndStatusAndBusinessIdIsNull(influencer.getId(), MatchStatus.PENDING)
                .stream()
                .map(m -> {
                    Match match = m.toMatch();
                    CampaignEntity c = m.getCampaign();
                    if (c != null) {
                        match.setCampaignTitle(c.getTitle());
                        match.setCampaignDescription(c.getDescription());
                        match.setCampaignObjective(c.getObjective());
                        match.setCampaignRequirements(c.getRequirements());
                        match.setCampaignReward(c.getReward());
                        match.setCampaignStatus(c.getStatus() != null ? c.getStatus().name() : null);
                        match.setCampaignCreationDate(c.getCreationDate() != null ? c.getCreationDate().toString() : null);
                        BusinessEntity b = c.getBusiness();
                        if (b != null) {
                            match.setBusinessName(b.getName());
                            match.setBusinessCategory(b.getCategory());
                            match.setBusinessDescription(b.getDescription());
                            match.setBusinessWebsite(b.getWebsite());
                            match.setBusinessProvince(b.getProvince());
                            match.setBusinessAddress(b.getAddress());
                            match.setBusinessVerified(b.getVerified());
                        }
                    }
                    return match;
                })
                .toList();
    }
}
