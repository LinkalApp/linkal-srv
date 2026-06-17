package es.miw.tfm.linkal.domain.persistence;

import es.miw.tfm.linkal.domain.model.Chat;
import es.miw.tfm.linkal.domain.model.Message;

import java.util.List;
import java.util.UUID;

public interface ChatPersistence {
    Chat createByMatch(UUID matchId);
    List<Chat> findAllByUser(String email);
    Message sendMessage(UUID chatId, String text, String senderEmail);
}
