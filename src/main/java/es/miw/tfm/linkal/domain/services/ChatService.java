package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Chat;
import es.miw.tfm.linkal.domain.model.Message;
import es.miw.tfm.linkal.domain.persistence.ChatPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatPersistence chatPersistence;

    public Chat createByMatch(UUID matchId) {
        return chatPersistence.createByMatch(matchId);
    }

    public List<Chat> findAllByUser(String email) {
        return chatPersistence.findAllByUser(email);
    }

    public Message sendMessage(UUID chatId, String text, String senderEmail) {
        return chatPersistence.sendMessage(chatId, text, senderEmail);
    }
}
