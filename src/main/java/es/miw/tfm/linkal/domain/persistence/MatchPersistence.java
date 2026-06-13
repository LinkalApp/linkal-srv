package es.miw.tfm.linkal.domain.persistence;

import es.miw.tfm.linkal.domain.model.Match;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface MatchPersistence {
    Match createByInfluencer(UUID campaignId, String influencerEmail);
    Optional<Match> findByInfluencer(UUID campaignId, String influencerEmail);
    Match createByBusiness(UUID influencerId, UUID campaignId, String businessEmail);
    List<Match> findPendingByInfluencer(String influencerEmail);
    List<Match> findPendingByBusiness(String businessEmail);
    List<Match> findCompletedByInfluencer(String influencerEmail);
    List<Match> findCompletedByBusiness(String businessEmail);
}
