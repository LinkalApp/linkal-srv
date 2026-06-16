package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.BadRequestException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Chat;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import es.miw.tfm.linkal.domain.persistence.ChatPersistence;
import es.miw.tfm.linkal.domain.services.ChatService;
import es.miw.tfm.linkal.infrastructure.jpa.entities.CampaignEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.ChatEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.InfluencerEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.MatchEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.ChatRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ChatPersistenceJpaTest {
    @Mock private ChatRepository chatRepository;
    @Mock private MatchRepository matchRepository;

    @InjectMocks
    private ChatPersistenceJpa chatPersistenceJpa;

    // ------------------------------------------------------------------------
    //  createForMatch — errores
    // -------------------------------------------------------------------------

    @Test
    void createForMatch_shouldThrowNotFound_whenMatchDoesNotExist() {
        UUID matchId = UUID.randomUUID();
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> chatPersistenceJpa.createByMatch(matchId));
        verify(chatRepository, never()).save(any());
    }

    @Test
    void createForMatch_shouldThrowBadRequest_whenMatchIsPending() {
        UUID matchId = UUID.randomUUID();
        MatchEntity match = buildMatch(matchId, MatchStatus.PENDING);
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        assertThrows(BadRequestException.class,
                () -> chatPersistenceJpa.createByMatch(matchId));
        verify(chatRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    //  createForMatch — creación nueva
    // -------------------------------------------------------------------------

    @Test
    void createForMatch_shouldCreateChat_whenMatchIsCompletedAndNoChatExists() {
        UUID matchId = UUID.randomUUID();
        MatchEntity match = buildCompletedMatch(matchId);
        ChatEntity saved = buildChatEntity(match);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(chatRepository.findByMatch_Id(matchId)).thenReturn(Optional.empty());
        when(chatRepository.save(any())).thenReturn(saved);

        Chat result = chatPersistenceJpa.createByMatch(matchId);

        assertNotNull(result);
        verify(chatRepository).save(any(ChatEntity.class));
    }

    @Test
    void createForMatch_shouldPersistChatLinkedToMatch() {
        UUID matchId = UUID.randomUUID();
        MatchEntity match = buildCompletedMatch(matchId);
        ChatEntity saved = buildChatEntity(match);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(chatRepository.findByMatch_Id(matchId)).thenReturn(Optional.empty());
        when(chatRepository.save(any())).thenReturn(saved);

        chatPersistenceJpa.createByMatch(matchId);

        ArgumentCaptor<ChatEntity> captor = ArgumentCaptor.forClass(ChatEntity.class);
        verify(chatRepository).save(captor.capture());
        assertEquals(match, captor.getValue().getMatch());
    }

    @Test
    void createForMatch_shouldBuildChatNameFromCampaignAndInfluencer() {
        UUID matchId = UUID.randomUUID();
        CampaignEntity campaign = buildCampaign("Campaña Verano");
        InfluencerEntity influencer = buildInfluencer("Ana López");
        MatchEntity match = buildCompletedMatchWithDetails(matchId, campaign, influencer);
        ChatEntity saved = buildChatEntity(match);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(chatRepository.findByMatch_Id(matchId)).thenReturn(Optional.empty());
        when(chatRepository.save(any())).thenReturn(saved);

        chatPersistenceJpa.createByMatch(matchId);

        ArgumentCaptor<ChatEntity> captor = ArgumentCaptor.forClass(ChatEntity.class);
        verify(chatRepository).save(captor.capture());
        assertEquals("Campaña Verano · Ana López", captor.getValue().getName());
    }

    @Test
    void createForMatch_shouldUseFallbackName_whenCampaignIsNull() {
        UUID matchId = UUID.randomUUID();
        MatchEntity match = buildMatch(matchId, MatchStatus.COMPLETED);
        match.setCampaign(null);
        match.setInfluencer(buildInfluencer("María"));
        ChatEntity saved = buildChatEntity(match);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(chatRepository.findByMatch_Id(matchId)).thenReturn(Optional.empty());
        when(chatRepository.save(any())).thenReturn(saved);

        chatPersistenceJpa.createByMatch(matchId);

        ArgumentCaptor<ChatEntity> captor = ArgumentCaptor.forClass(ChatEntity.class);
        verify(chatRepository).save(captor.capture());
        assertTrue(captor.getValue().getName().startsWith("Campaña"));
    }

    @Test
    void createForMatch_shouldUseFallbackName_whenInfluencerIsNull() {
        UUID matchId = UUID.randomUUID();
        MatchEntity match = buildMatch(matchId, MatchStatus.COMPLETED);
        match.setCampaign(buildCampaign("Mi Campaña"));
        match.setInfluencer(null);
        ChatEntity saved = buildChatEntity(match);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(chatRepository.findByMatch_Id(matchId)).thenReturn(Optional.empty());
        when(chatRepository.save(any())).thenReturn(saved);

        chatPersistenceJpa.createByMatch(matchId);

        ArgumentCaptor<ChatEntity> captor = ArgumentCaptor.forClass(ChatEntity.class);
        verify(chatRepository).save(captor.capture());
        assertTrue(captor.getValue().getName().endsWith("Influencer"));
    }

    // ------------------------------------------------------------------------
    //  createForMatch — chat duplicado
    // -------------------------------------------------------------------------

    @Test
    void createForMatch_shouldReturnExistingChat_whenAlreadyExists() {
        UUID matchId = UUID.randomUUID();
        MatchEntity match = buildCompletedMatch(matchId);
        ChatEntity existing = buildChatEntity(match);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(chatRepository.findByMatch_Id(matchId)).thenReturn(Optional.of(existing));

        Chat result = chatPersistenceJpa.createByMatch(matchId);

        assertNotNull(result);
        assertEquals(existing.getId(), result.getId());
    }

    @Test
    void createForMatch_shouldNotSave_whenChatAlreadyExists() {
        UUID matchId = UUID.randomUUID();
        MatchEntity match = buildCompletedMatch(matchId);
        ChatEntity existing = buildChatEntity(match);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(chatRepository.findByMatch_Id(matchId)).thenReturn(Optional.of(existing));

        chatPersistenceJpa.createByMatch(matchId);

        verify(chatRepository, never()).save(any());
    }

    // ------------------------------------------------------------------------
    //  helpers
    // ------------------------------------------------------------------------

    private MatchEntity buildMatch(UUID id, MatchStatus status) {
        MatchEntity m = new MatchEntity();
        m.setId(id);
        m.setStatus(status);
        m.setCreatedAt(LocalDateTime.now());
        return m;
    }

    private MatchEntity buildCompletedMatch(UUID id) {
        MatchEntity m = buildMatch(id, MatchStatus.COMPLETED);
        m.setMatchedAt(LocalDateTime.now());
        return m;
    }

    private MatchEntity buildCompletedMatchWithDetails(UUID id, CampaignEntity campaign, InfluencerEntity influencer) {
        MatchEntity m = buildCompletedMatch(id);
        m.setCampaign(campaign);
        m.setInfluencer(influencer);
        return m;
    }

    private CampaignEntity buildCampaign(String title) {
        CampaignEntity c = new CampaignEntity();
        c.setId(UUID.randomUUID());
        c.setTitle(title);
        return c;
    }

    private InfluencerEntity buildInfluencer(String name) {
        InfluencerEntity i = new InfluencerEntity();
        i.setId(UUID.randomUUID());
        i.setName(name);
        return i;
    }

    private ChatEntity buildChatEntity(MatchEntity match) {
        return ChatEntity.builder()
                .id(UUID.randomUUID())
                .name("Chat test")
                .match(match)
                .build();
    }
}
