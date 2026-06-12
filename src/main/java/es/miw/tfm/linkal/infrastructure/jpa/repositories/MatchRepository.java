package es.miw.tfm.linkal.infrastructure.jpa.repositories;

import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import es.miw.tfm.linkal.infrastructure.jpa.entities.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface MatchRepository extends JpaRepository<MatchEntity, UUID> {
    Optional<MatchEntity> findByCampaign_IdAndInfluencer_Id(UUID campaignId, UUID influencerId);
    List<MatchEntity> findByInfluencer_IdAndStatusAndBusinessIdIsNull(UUID influencerId, MatchStatus status);
}
