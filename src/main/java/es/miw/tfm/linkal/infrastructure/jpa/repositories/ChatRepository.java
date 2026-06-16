package es.miw.tfm.linkal.infrastructure.jpa.repositories;

import es.miw.tfm.linkal.infrastructure.jpa.entities.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<ChatEntity, UUID> {
    Optional<ChatEntity> findByMatch_Id(UUID matchId);
}
