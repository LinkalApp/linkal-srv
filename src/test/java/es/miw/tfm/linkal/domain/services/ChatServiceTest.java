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

    // helpers --------------------------------------------------------------------

    private Chat buildChat(UUID matchId) {
        return Chat.builder()
                .id(UUID.randomUUID())
                .name("Chat test")
                .matchId(matchId)
                .build();
    }
}
