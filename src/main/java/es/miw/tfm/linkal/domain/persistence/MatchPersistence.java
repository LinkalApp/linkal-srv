package es.miw.tfm.linkal.domain.persistence;

import es.miw.tfm.linkal.domain.model.Match;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchPersistence {
    Match createByInfluencer(UUID campaignId, String influencerEmail);
    Optional<Match> findByInfluencer(UUID campaignId, String influencerEmail);
    Match createByBusiness(UUID influencerId, UUID campaignId, String businessEmail);
}
