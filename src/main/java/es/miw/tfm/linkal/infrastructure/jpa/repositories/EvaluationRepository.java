package es.miw.tfm.linkal.infrastructure.jpa.repositories;

import es.miw.tfm.linkal.infrastructure.jpa.entities.EvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface EvaluationRepository extends JpaRepository<EvaluationEntity, UUID> {
    @Query(value = "SELECT AVG(score) FROM evaluations WHERE id_user_valued = :influencerId", nativeQuery = true)
    Double findAverageScoreByValuedUserId(@Param("influencerId") UUID influencerId);
}
