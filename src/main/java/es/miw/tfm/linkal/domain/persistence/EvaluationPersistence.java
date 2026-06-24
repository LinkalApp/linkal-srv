package es.miw.tfm.linkal.domain.persistence;

import es.miw.tfm.linkal.domain.model.Evaluation;

import java.util.UUID;

public interface EvaluationPersistence {
    Double averageScoreByInfluencerId(UUID influencerId);
    Double averageScoreByBusinessId(UUID businessId);
    Evaluation create(Evaluation evaluation, UUID matchId, String businessEmail);
}