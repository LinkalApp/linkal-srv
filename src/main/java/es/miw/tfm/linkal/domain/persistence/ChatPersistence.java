package es.miw.tfm.linkal.domain.persistence;

import es.miw.tfm.linkal.domain.model.Chat;

import java.util.UUID;

public interface ChatPersistence {
    Chat createByMatch(UUID matchId);
}
