package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.Message;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MessageEntityTest {

    // -------------------------------------------------------------------------
    //  toMessage()
    // -------------------------------------------------------------------------

    @Test
    void toMessage_shouldMapIdTextAndSentAt() {
        UUID id = UUID.randomUUID();
        LocalDateTime sentAt = LocalDateTime.of(2025, 5, 10, 12, 0);

        MessageEntity entity = MessageEntity.builder()
                .id(id)
                .text("Hola, ¿cómo estás?")
                .sentAt(sentAt)
                .senderId(UUID.randomUUID())
                .build();

        Message message = entity.toMessage();

        assertEquals(id, message.getId());
        assertEquals("Hola, ¿cómo estás?", message.getText());
        assertEquals(sentAt, message.getSentAt());
    }

    @Test
    void toMessage_shouldSetChatId_whenChatIsNotNull() {
        UUID chatId = UUID.randomUUID();
        ChatEntity chat = ChatEntity.builder()
                .id(chatId)
                .name("Chat de prueba")
                .build();

        MessageEntity entity = MessageEntity.builder()
                .id(UUID.randomUUID())
                .text("Mensaje de prueba")
                .sentAt(LocalDateTime.now())
                .senderId(UUID.randomUUID())
                .chat(chat)
                .build();

        Message message = entity.toMessage();

        assertEquals(chatId, message.getChatId());
    }

    @Test
    void toMessage_shouldLeaveChatIdNull_whenChatIsNull() {
        MessageEntity entity = MessageEntity.builder()
                .id(UUID.randomUUID())
                .text("Mensaje sin chat")
                .sentAt(LocalDateTime.now())
                .senderId(UUID.randomUUID())
                .build();

        Message message = entity.toMessage();

        assertNull(message.getChatId());
    }

    @Test
    void toMessage_shouldReturnNewInstanceEachTime() {
        MessageEntity entity = MessageEntity.builder()
                .id(UUID.randomUUID())
                .text("Test")
                .sentAt(LocalDateTime.now())
                .senderId(UUID.randomUUID())
                .build();

        Message m1 = entity.toMessage();
        Message m2 = entity.toMessage();

        assertNotSame(m1, m2);
    }
}
