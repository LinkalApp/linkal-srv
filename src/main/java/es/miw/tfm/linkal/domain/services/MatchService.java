package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Match;
import es.miw.tfm.linkal.domain.persistence.MatchPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchPersistence matchPersistence;

    public Match createByInfluencer(UUID campaignId, String influencerEmail) {
        return matchPersistence.createByInfluencer(campaignId, influencerEmail);
    }

    public Optional<Match> findByInfluencer(UUID campaignId, String influencerEmail) {
        return matchPersistence.findByInfluencer(campaignId, influencerEmail);
    }
}
