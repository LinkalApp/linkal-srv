package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Evaluation;
import es.miw.tfm.linkal.domain.persistence.EvaluationPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationPersistence evaluationPersistence;

    public Evaluation create(Evaluation evaluation, UUID matchId, String businessEmail) {
        return evaluationPersistence.create(evaluation, matchId, businessEmail);
    }

    public Evaluation createByInfluencer(Evaluation evaluation, UUID matchId, String influencerEmail) {
        return evaluationPersistence.createByInfluencer(evaluation, matchId, influencerEmail);
    }
}
