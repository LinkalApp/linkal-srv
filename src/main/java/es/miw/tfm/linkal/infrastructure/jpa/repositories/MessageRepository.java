package es.miw.tfm.linkal.infrastructure.jpa.repositories;

import es.miw.tfm.linkal.infrastructure.jpa.entities.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {
    Optional<MessageEntity> findTopByChat_IdOrderBySentAtDesc(UUID chatId);
    List<MessageEntity> findByChat_IdOrderBySentAtAsc(UUID chatId);
}
