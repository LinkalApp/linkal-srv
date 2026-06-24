package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.exceptions.ForbiddenException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Evaluation;
import es.miw.tfm.linkal.domain.model.enums.CampaignStatus;
import es.miw.tfm.linkal.domain.persistence.EvaluationPersistence;
import es.miw.tfm.linkal.infrastructure.jpa.entities.BusinessEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.EvaluationEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.MatchEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.BusinessRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.EvaluationRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EvaluationPersistenceJpa implements EvaluationPersistence {

    private final EvaluationRepository evaluationRepository;
    private final MatchRepository matchRepository;
    private final BusinessRepository businessRepository;

    @Override
    public Double averageScoreByInfluencerId(UUID influencerId) {
        return evaluationRepository.findAverageScoreByValuedUserId(influencerId);
    }

    @Override
    public Double averageScoreByBusinessId(UUID businessId) {
        return evaluationRepository.findAverageScoreByValuedUserId(businessId);
    }

    @Override
    @Transactional
    public Evaluation create(Evaluation evaluation, UUID matchId, String businessEmail) {
        BusinessEntity business = businessRepository.findByEmail(businessEmail)
                .orElseThrow(() -> new NotFoundException("Business not found: " + businessEmail));

        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found: " + matchId));

        if (!match.getCampaign().getBusiness().getId().equals(business.getId())) {
            throw new ForbiddenException("No tienes permiso para valorar este match");
        }

        CampaignStatus campaignStatus = match.getCampaign().getStatus();
        if (campaignStatus != CampaignStatus.CLOSED) {
            throw new ConflictException("La campaña debe estar CLOSED para poder valorar");
        }

        boolean alreadyRated = match.getEvaluations().stream()
                .anyMatch(e -> e.getIdUserValued().equals(match.getInfluencer().getId()));
        if (alreadyRated) {
            throw new ConflictException("Ya has valorado al influencer de este match");
        }

        EvaluationEntity entity = EvaluationEntity.builder()
                .score(evaluation.getScore())
                .idUserValued(match.getInfluencer().getId())
                .match(match)
                .build();

        Evaluation saved = evaluationRepository.save(entity).toEvaluation();
        return saved;
    }
}