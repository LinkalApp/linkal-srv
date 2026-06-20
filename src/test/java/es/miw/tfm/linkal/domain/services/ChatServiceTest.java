package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.exceptions.BadRequestException;
import es.miw.tfm.linkal.domain.exceptions.ForbiddenException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Chat;
import es.miw.tfm.linkal.domain.model.Message;
import es.miw.tfm.linkal.domain.persistence.ChatPersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {
    @Mock
    private ChatPersistence chatPersistence;
    @InjectMocks
    private ChatService chatService;

    @Test
    void createForMatch_shouldDelegateToPersistence() {
        UUID matchId = UUID.randomUUID();
        Chat chat = buildChat(matchId);
        when(chatPersistence.createByMatch(matchId)).thenReturn(chat);

        chatService.createByMatch(matchId);

        verify(chatPersistence).createByMatch(matchId);
    }

    // createByMatch ------------------------------------------------------------------

    @Test
    void createForMatch_shouldReturnChatFromPersistence() {
        UUID matchId = UUID.randomUUID();
        Chat chat = buildChat(matchId);
        when(chatPersistence.createByMatch(matchId)).thenReturn(chat);

        Chat result = chatService.createByMatch(matchId);

        assertNotNull(result);
        assertEquals(chat.getId(), result.getId());
        assertEquals(chat.getMatchId(), result.getMatchId());
    }

    @Test
    void createForMatch_shouldPropagateNotFoundException() {
        UUID matchId = UUID.randomUUID();
        when(chatPersistence.createByMatch(matchId)).thenThrow(new NotFoundException("Match not found"));

        assertThrows(NotFoundException.class, () -> chatService.createByMatch(matchId));
    }

    @Test
    void createForMatch_shouldPropagateBadRequestException() {
        UUID matchId = UUID.randomUUID();
        when(chatPersistence.createByMatch(matchId))
                .thenThrow(new BadRequestException("Match no está COMPLETED"));

        assertThrows(BadRequestException.class, () -> chatService.createByMatch(matchId));
    }

    @Test
    void createForMatch_shouldPassExactMatchIdToPersistence() {
        UUID matchId = UUID.randomUUID();
        when(chatPersistence.createByMatch(any())).thenReturn(buildChat(matchId));

        chatService.createByMatch(matchId);

        verify(chatPersistence).createByMatch(matchId);
        verify(chatPersistence, times(1)).createByMatch(any());
    }

    // findAllByUser -----------------------------------------------------------

    @Test
    void findAllByUser_shouldDelegateToPersistence() {
        when(chatPersistence.findAllByUser("user@test.com")).thenReturn(List.of());

        chatService.findAllByUser("user@test.com");

        verify(chatPersistence).findAllByUser("user@test.com");
    }

    @Test
    void findAllByUser_shouldReturnEmptyList() {
        when(chatPersistence.findAllByUser("user@test.com")).thenReturn(List.of());

        assertTrue(chatService.findAllByUser("user@test.com").isEmpty());
    }

    @Test
    void findAllByUser_shouldReturnChatsFromPersistence() {
        UUID matchId = UUID.randomUUID();
        Chat chat = buildChatWithDisplayName(matchId, "Nike Spain");
        when(chatPersistence.findAllByUser("user@test.com")).thenReturn(List.of(chat));

        List<Chat> result = chatService.findAllByUser("user@test.com");

        assertEquals(1, result.size());
        assertEquals("Nike Spain", result.get(0).getDisplayName());
    }

    @Test
    void findAllByUser_shouldPropagateNotFoundException() {
        when(chatPersistence.findAllByUser("unknown@test.com"))
                .thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class,
                () -> chatService.findAllByUser("unknown@test.com"));
    }

    @Test
    void findAllByUser_shouldNotCallCreateForMatch() {
        when(chatPersistence.findAllByUser(any())).thenReturn(List.of());

        chatService.findAllByUser("user@test.com");

        verify(chatPersistence, never()).createByMatch(any());
    }

    // sendMessage --------------------------------------------------------------------------

    @Test
    void sendMessage_shouldDelegateToPersistence() {
        UUID chatId = UUID.randomUUID();
        when(chatPersistence.sendMessage(chatId, "Hola!", "user@test.com"))
                .thenReturn(buildMessage(chatId, "Hola!"));

        chatService.sendMessage(chatId, "Hola!", "user@test.com");

        verify(chatPersistence).sendMessage(chatId, "Hola!", "user@test.com");
    }

    @Test
    void sendMessage_shouldReturnMessageFromPersistence() {
        UUID chatId = UUID.randomUUID();
        Message msg = buildMessage(chatId, "Hola!");
        when(chatPersistence.sendMessage(chatId, "Hola!", "user@test.com")).thenReturn(msg);

        Message result = chatService.sendMessage(chatId, "Hola!", "user@test.com");

        assertNotNull(result);
        assertEquals("Hola!", result.getText());
    }

    @Test
    void sendMessage_shouldPropagateForbiddenException() {
        UUID chatId = UUID.randomUUID();
        when(chatPersistence.sendMessage(any(), any(), eq("outsider@test.com")))
                .thenThrow(new ForbiddenException("No tienes acceso"));

        assertThrows(ForbiddenException.class,
                () -> chatService.sendMessage(chatId, "Hola!", "outsider@test.com"));
    }

    @Test
    void sendMessage_shouldPropagateNotFoundException() {
        UUID chatId = UUID.randomUUID();
        when(chatPersistence.sendMessage(any(), any(), any()))
                .thenThrow(new NotFoundException("Chat not found"));

        assertThrows(NotFoundException.class,
                () -> chatService.sendMessage(chatId, "Hola!", "user@test.com"));
    }

    // getMessages --------------------------------------------------------------------

    @Test
    void getMessages_shouldDelegateToPersistence() {
        UUID chatId = UUID.randomUUID();
        when(chatPersistence.getMessages(chatId, "user@test.com")).thenReturn(List.of());
        chatService.getMessages(chatId, "user@test.com");
        verify(chatPersistence).getMessages(chatId, "user@test.com");
    }

    @Test
    void getMessages_shouldReturnEmptyList_whenNoMessages() {
        UUID chatId = UUID.randomUUID();
        when(chatPersistence.getMessages(chatId, "user@test.com")).thenReturn(List.of());
        List<Message> result = chatService.getMessages(chatId, "user@test.com");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getMessages_shouldReturnMessagesFromPersistence() {
        UUID chatId = UUID.randomUUID();
        Message m1 = buildMessage(chatId, "Hola!");
        Message m2 = buildMessage(chatId, "Todo bien?");
        when(chatPersistence.getMessages(chatId, "user@test.com")).thenReturn(List.of(m1, m2));
        List<Message> result = chatService.getMessages(chatId, "user@test.com");
        assertEquals(2, result.size());
        assertEquals("Hola!", result.get(0).getText());
        assertEquals("Todo bien?", result.get(1).getText());
    }

    @Test
    void getMessages_shouldPropagateForbiddenException() {
        UUID chatId = UUID.randomUUID();
        when(chatPersistence.getMessages(any(), eq("outsider@test.com")))
                .thenThrow(new ForbiddenException("No tienes acceso"));
        assertThrows(ForbiddenException.class,
                () -> chatService.getMessages(chatId, "outsider@test.com"));
    }

    @Test
    void getMessages_shouldPropagateNotFoundException_whenChatNotFound() {
        UUID chatId = UUID.randomUUID();
        when(chatPersistence.getMessages(any(), any()))
                .thenThrow(new NotFoundException("Chat not found"));
        assertThrows(NotFoundException.class,
                () -> chatService.getMessages(chatId, "user@test.com"));
    }

    @Test
    void getMessages_shouldNotCallSendMessage() {
        UUID chatId = UUID.randomUUID();
        when(chatPersistence.getMessages(any(), any())).thenReturn(List.of());
        chatService.getMessages(chatId, "user@test.com");
        verify(chatPersistence, never()).sendMessage(any(), any(), any());
    }

    // helpers --------------------------------------------------------------------

    private Chat buildChat(UUID matchId) {
        return Chat.builder()
                .id(UUID.randomUUID())
                .name("Chat test")
                .matchId(matchId)
                .build();
    }

    private Chat buildChatWithDisplayName(UUID matchId, String displayName) {
        return Chat.builder()
                .id(UUID.randomUUID())
                .name("Chat test")
                .matchId(matchId)
                .displayName(displayName)
                .build();
    }

    private Message buildMessage(UUID chatId, String text) {
        return Message.builder()
                .id(UUID.randomUUID())
                .text(text)
                .sentAt(LocalDateTime.now())
                .chatId(chatId)
                .senderId(UUID.randomUUID())
                .build();
    }
}
