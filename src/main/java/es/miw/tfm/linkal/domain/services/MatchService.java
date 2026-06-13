package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Match;
import es.miw.tfm.linkal.domain.persistence.MatchPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public Match createByBusiness(UUID influencerId, UUID campaignId, String businessEmail) {
        return matchPersistence.createByBusiness(influencerId, campaignId, businessEmail);
    }

    public List<Match> findPendingByInfluencer(String influencerEmail) {
        return matchPersistence.findPendingByInfluencer(influencerEmail);
    }

    public List<Match> findPendingByBusiness(String businessEmail) {
        return matchPersistence.findPendingByBusiness(businessEmail);
    }

    public List<Match> findCompletedByInfluencer(String influencerEmail) {
        return matchPersistence.findCompletedByInfluencer(influencerEmail);
    }

    public List<Match> findCompletedByBusiness(String businessEmail) {
        return matchPersistence.findCompletedByBusiness(businessEmail);
    }
}
