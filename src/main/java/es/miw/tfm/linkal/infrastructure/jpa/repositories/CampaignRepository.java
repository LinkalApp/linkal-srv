package es.miw.tfm.linkal.infrastructure.jpa.repositories;

import es.miw.tfm.linkal.domain.model.enums.CampaignStatus;
import es.miw.tfm.linkal.infrastructure.jpa.entities.CampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CampaignRepository extends JpaRepository<CampaignEntity, UUID> {
    List<CampaignEntity> findAllByBusinessId(UUID businessId);
    List<CampaignEntity> findAllByStatus(CampaignStatus status);

    @Query("SELECT c FROM CampaignEntity c WHERE c.status = 'OPEN' " +
            "AND (:category IS NULL OR c.business.category = :category) " +
            "AND (:province IS NULL OR c.business.province = :province)")
    List<CampaignEntity> findOpenByFilters(@Param("category") String category, @Param("province") String province);

    @Query("SELECT c FROM CampaignEntity c WHERE c.status = 'OPEN' " +
            "AND c.business.category NOT IN :standardCategories " +
            "AND (:province IS NULL OR c.business.province = :province)")
    List<CampaignEntity> findOpenByOtherCategories( @Param("standardCategories") List<String> standardCategories,
                                                    @Param("province") String province);
}
