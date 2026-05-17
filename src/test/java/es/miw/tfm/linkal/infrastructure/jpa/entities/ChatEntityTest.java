package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.Chat;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ChatEntityTest {

    // -------------------------------------------------------------------------
    //  toChat()
    // -------------------------------------------------------------------------

    @Test
    void toChat_shouldMapIdAndName() {
        UUID id = UUID.randomUUID();

        ChatEntity entity = ChatEntity.builder()
                .id(id)
                .name("Chat de prueba")
                .build();

        Chat chat = entity.toChat();

        assertEquals(id, chat.getId());
        assertEquals("Chat de prueba", chat.getName());
    }

    @Test
    void toChat_shouldSetMatchId_whenMatchIsNotNull() {
        UUID matchId = UUID.randomUUID();
        MatchEntity match = MatchEntity.builder()
                .id(matchId)
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.PENDING)
                .build();

        ChatEntity entity = ChatEntity.builder()
                .id(UUID.randomUUID())
                .name("Chat")
                .match(match)
                .build();

        Chat chat = entity.toChat();

        assertEquals(matchId, chat.getMatchId());
    }

    @Test
    void toChat_shouldLeaveMatchIdNull_whenMatchIsNull() {
        ChatEntity entity = ChatEntity.builder()
                .id(UUID.randomUUID())
                .name("Chat sin match")
                .build();

        Chat chat = entity.toChat();

        assertNull(chat.getMatchId());
    }

    @Test
    void toChat_shouldSetCampaignId_whenCampaignIsNotNull() {
        UUID campaignId = UUID.randomUUID();
        CampaignEntity campaign = new CampaignEntity();
        campaign.setId(campaignId);

        ChatEntity entity = ChatEntity.builder()
                .id(UUID.randomUUID())
                .name("Chat")
                .campaign(campaign)
                .build();

        Chat chat = entity.toChat();

        assertEquals(campaignId, chat.getCampaignId());
    }

    @Test
    void toChat_shouldLeaveCampaignIdNull_whenCampaignIsNull() {
        ChatEntity entity = ChatEntity.builder()
                .id(UUID.randomUUID())
                .name("Chat sin campaña")
                .build();

        Chat chat = entity.toChat();

        assertNull(chat.getCampaignId());
    }

    @Test
    void toChat_shouldReturnNewInstanceEachTime() {
        ChatEntity entity = ChatEntity.builder()
                .id(UUID.randomUUID())
                .name("Chat")
                .build();

        Chat c1 = entity.toChat();
        Chat c2 = entity.toChat();

        assertNotSame(c1, c2);
    }
}
