package es.miw.tfm.linkal.domain.persistence;

import java.util.UUID;

public interface EvaluationPersistence {
    Double averageScoreByInfluencerId(UUID influencerId);
    Double averageScoreByBusinessId(UUID businessId);
}