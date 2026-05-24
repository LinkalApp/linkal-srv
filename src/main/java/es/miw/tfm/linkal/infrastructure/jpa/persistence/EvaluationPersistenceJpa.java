package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.persistence.EvaluationPersistence;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.EvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EvaluationPersistenceJpa implements EvaluationPersistence {

    private final EvaluationRepository evaluationRepository;

    @Override
    public Double averageScoreByInfluencerId(UUID influencerId) {
        return evaluationRepository.findAverageScoreByValuedUserId(influencerId);
    }
}