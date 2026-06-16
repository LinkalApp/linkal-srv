package es.miw.tfm.linkal.infrastructure.jpa.repositories;

import es.miw.tfm.linkal.infrastructure.jpa.entities.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<ChatEntity, UUID> {
    Optional<ChatEntity> findByMatch_Id(UUID matchId);

    @Query("""
            SELECT c FROM ChatEntity c
            WHERE (c.match.influencer.id = :userId OR c.match.campaign.business.id = :userId)
              AND c.match.status = 'COMPLETED'
            """)
    List<ChatEntity> findAllByUserId(@Param("userId") UUID userId);
}
