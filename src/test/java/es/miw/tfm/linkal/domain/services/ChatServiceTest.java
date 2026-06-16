package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.exceptions.BadRequestException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Chat;
import es.miw.tfm.linkal.domain.persistence.ChatPersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
