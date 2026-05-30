package es.miw.tfm.linkal.infrastructure.jpa.repositories;

import es.miw.tfm.linkal.infrastructure.jpa.entities.CampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CampaignRepository extends JpaRepository<CampaignEntity, UUID> {
    List<CampaignEntity> findAllByBusinessId(UUID businessId);
}
