package es.miw.tfm.linkal.infrastructure.jpa.repositories;

import es.miw.tfm.linkal.infrastructure.jpa.entities.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<MatchEntity, UUID> {
    Optional<MatchEntity> findByCampaign_IdAndInfluencer_Id(UUID campaignId, UUID influencerId);
}
