package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Influencer;
import es.miw.tfm.linkal.domain.persistence.EvaluationPersistence;
import es.miw.tfm.linkal.domain.persistence.InfluencerPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InfluencerService {
    private final InfluencerPersistence influencerPersistence;
    private final EvaluationPersistence evaluationPersistence;
    private final PasswordEncoder passwordEncoder;

    public Influencer create(Influencer influencer) {
        influencer.setPassword(passwordEncoder.encode(influencer.getPassword()));
        return this.influencerPersistence.create(influencer);
    }

    public Influencer readMe(String email) {
        Influencer influencer = influencerPersistence.readMe(email);
        influencer.setPassword(null);
        influencer.setAverageRating(evaluationPersistence.averageScoreByInfluencerId(influencer.getId()));
        return influencer;
    }
}
