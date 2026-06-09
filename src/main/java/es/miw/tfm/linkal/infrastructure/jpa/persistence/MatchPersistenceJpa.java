package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Match;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import es.miw.tfm.linkal.domain.persistence.MatchPersistence;
import es.miw.tfm.linkal.infrastructure.jpa.entities.CampaignEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.InfluencerEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.MatchEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.CampaignRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.InfluencerRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MatchPersistenceJpa implements MatchPersistence {

    private final MatchRepository matchRepository;
    private final CampaignRepository campaignRepository;
    private final InfluencerRepository influencerRepository;

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
            throw new ConflictException("Ya has expresado interés en esta campaña");
        }

        Optional<MatchEntity> byBusiness = matchRepository
                .findByCampaign_IdAndInfluencerIsNull(campaignId);

        if (byBusiness.isPresent()) {
            MatchEntity existing = byBusiness.get();
            existing.setInfluencer(influencer);
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
}
