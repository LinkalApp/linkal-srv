package es.miw.tfm.linkal.infrastructure.jpa.repositories;

import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import es.miw.tfm.linkal.infrastructure.jpa.entities.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface MatchRepository extends JpaRepository<MatchEntity, UUID> {
    Optional<MatchEntity> findByCampaign_IdAndInfluencer_Id(UUID campaignId, UUID influencerId);

    @Query("SELECT m FROM MatchEntity m WHERE m.influencer.id = :influencerId AND m.status = :status AND m.businessId IS NULL")
    List<MatchEntity> findPendingByInfluencer(UUID influencerId, MatchStatus status);

    @Query("SELECT m FROM MatchEntity m WHERE m.campaign.business.id = :businessId AND m.status = :status AND m.businessId IS NOT NULL")
    List<MatchEntity> findPendingByBusiness(UUID businessId, MatchStatus status);

    @Query("SELECT m FROM MatchEntity m WHERE m.influencer.id = :influencerId AND m.status = :status")
    List<MatchEntity> findCompletedByInfluencer(UUID influencerId, MatchStatus status);

    @Query("SELECT m FROM MatchEntity m WHERE m.campaign.business.id = :businessId AND m.status = :status")
    List<MatchEntity> findCompletedByBusiness(UUID businessId, MatchStatus status);

    @Query("SELECT m FROM MatchEntity m WHERE m.campaign.id = :campaignId AND m.status = :status")
    List<MatchEntity> findByCampaign_IdAndStatus(@Param("campaignId") UUID campaignId, @Param("status") MatchStatus status);

    List<MatchEntity> findAllByCampaign_Id(UUID campaignId);
}
