package es.miw.tfm.linkal.infrastructure.jpa.repositories;

import es.miw.tfm.linkal.infrastructure.jpa.entities.InfluencerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InfluencerRepository extends JpaRepository<InfluencerEntity, UUID> {
    Optional<InfluencerEntity> findByEmail(String email);

    @Query("SELECT DISTINCT i FROM InfluencerEntity i JOIN i.interests interest WHERE interest IN :interests")
    List<InfluencerEntity> findByInterestsIn(@Param("interests") List<String> interests);
}
