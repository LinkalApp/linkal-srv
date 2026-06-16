package es.miw.tfm.linkal.domain.persistence;

import es.miw.tfm.linkal.domain.model.Chat;

import java.util.List;
import java.util.UUID;

public interface ChatPersistence {
    Chat createByMatch(UUID matchId);
    List<Chat> findAllByUser(String email);
}
