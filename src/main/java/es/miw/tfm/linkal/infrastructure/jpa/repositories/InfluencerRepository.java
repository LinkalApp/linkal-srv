package es.miw.tfm.linkal.infrastructure.jpa.repositories;

import es.miw.tfm.linkal.infrastructure.jpa.entities.InfluencerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InfluencerRepository extends JpaRepository<InfluencerEntity, UUID> {
    Optional<InfluencerEntity> findByEmail(String email);
}
