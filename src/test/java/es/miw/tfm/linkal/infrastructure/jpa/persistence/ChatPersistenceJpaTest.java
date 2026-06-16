package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.BadRequestException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Chat;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import es.miw.tfm.linkal.domain.persistence.ChatPersistence;
import es.miw.tfm.linkal.domain.services.ChatService;
import es.miw.tfm.linkal.infrastructure.jpa.entities.*;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.ChatRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.MatchRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.MessageRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ChatPersistenceJpaTest {
    @Mock private ChatRepository chatRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private UserRepository userRepository;

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

    // -------------------------------------------------------------------------
    //  findAllByUser
    // -------------------------------------------------------------------------

    @Test
    void findAllByUser_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> chatPersistenceJpa.findAllByUser("unknown@test.com"));
    }

    @Test
    void findAllByUser_shouldReturnEmptyList_whenNoChats() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, "user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(chatRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<Chat> result = chatPersistenceJpa.findAllByUser("user@test.com");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByUser_shouldReturnChats_whenTheyExist() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, "user@test.com");
        InfluencerEntity influencer = buildInfluencer("Ana");
        influencer.setId(userId);
        MatchEntity match = buildCompletedMatchWithDetails(UUID.randomUUID(),
                buildCampaign("Campaña"), influencer);
        ChatEntity chatEntity = buildChatEntity(match);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(chatRepository.findAllByUserId(userId)).thenReturn(List.of(chatEntity));
        when(messageRepository.findTopByChat_IdOrderBySentAtDesc(chatEntity.getId()))
                .thenReturn(Optional.empty());

        List<Chat> result = chatPersistenceJpa.findAllByUser("user@test.com");

        assertEquals(1, result.size());
    }

    @Test
    void findAllByUser_shouldSetDisplayNameToBusiness_whenUserIsInfluencer() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, "influencer@test.com");
        InfluencerEntity influencer = buildInfluencer("Ana");
        influencer.setId(userId);
        BusinessEntity business = buildBusiness("Nike Spain");
        CampaignEntity campaign = buildCampaign("Campaña");
        campaign.setBusiness(business);
        MatchEntity match = buildCompletedMatchWithDetails(UUID.randomUUID(), campaign, influencer);
        ChatEntity chatEntity = buildChatEntity(match);

        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(user));
        when(chatRepository.findAllByUserId(userId)).thenReturn(List.of(chatEntity));
        when(messageRepository.findTopByChat_IdOrderBySentAtDesc(any())).thenReturn(Optional.empty());

        List<Chat> result = chatPersistenceJpa.findAllByUser("influencer@test.com");

        assertEquals("Nike Spain", result.get(0).getDisplayName());
    }

    @Test
    void findAllByUser_shouldSetDisplayNameToInfluencer_whenUserIsBusiness() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, "business@test.com");
        BusinessEntity business = buildBusiness("Nike Spain");
        business.setId(userId);
        InfluencerEntity influencer = buildInfluencer("Ana López");
        CampaignEntity campaign = buildCampaign("Campaña");
        campaign.setBusiness(business);
        MatchEntity match = buildCompletedMatchWithDetails(UUID.randomUUID(), campaign, influencer);
        ChatEntity chatEntity = buildChatEntity(match);

        when(userRepository.findByEmail("business@test.com")).thenReturn(Optional.of(user));
        when(chatRepository.findAllByUserId(userId)).thenReturn(List.of(chatEntity));
        when(messageRepository.findTopByChat_IdOrderBySentAtDesc(any())).thenReturn(Optional.empty());

        List<Chat> result = chatPersistenceJpa.findAllByUser("business@test.com");

        assertEquals("Ana López", result.get(0).getDisplayName());
    }

    @Test
    void findAllByUser_shouldPopulateLastMessage_whenMessagesExist() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, "user@test.com");
        InfluencerEntity influencer = buildInfluencer("Ana");
        influencer.setId(userId);
        MatchEntity match = buildCompletedMatchWithDetails(UUID.randomUUID(),
                buildCampaign("C"), influencer);
        ChatEntity chatEntity = buildChatEntity(match);
        MessageEntity lastMsg = buildMessage(chatEntity, "Hola!", LocalDateTime.now());

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(chatRepository.findAllByUserId(userId)).thenReturn(List.of(chatEntity));
        when(messageRepository.findTopByChat_IdOrderBySentAtDesc(chatEntity.getId()))
                .thenReturn(Optional.of(lastMsg));

        List<Chat> result = chatPersistenceJpa.findAllByUser("user@test.com");

        assertEquals("Hola!", result.get(0).getLastMessage());
        assertNotNull(result.get(0).getLastMessageAt());
    }

    @Test
    void findAllByUser_shouldLeaveLastMessageNull_whenNoMessages() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, "user@test.com");
        InfluencerEntity influencer = buildInfluencer("Ana");
        influencer.setId(userId);
        MatchEntity match = buildCompletedMatchWithDetails(UUID.randomUUID(),
                buildCampaign("C"), influencer);
        ChatEntity chatEntity = buildChatEntity(match);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(chatRepository.findAllByUserId(userId)).thenReturn(List.of(chatEntity));
        when(messageRepository.findTopByChat_IdOrderBySentAtDesc(chatEntity.getId()))
                .thenReturn(Optional.empty());

        List<Chat> result = chatPersistenceJpa.findAllByUser("user@test.com");

        assertNull(result.get(0).getLastMessage());
        assertNull(result.get(0).getLastMessageAt());
    }

    @Test
    void findAllByUser_shouldOrderByLastMessageAtDesc() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, "user@test.com");
        InfluencerEntity influencer = buildInfluencer("Ana");
        influencer.setId(userId);

        ChatEntity chatOld = buildChatEntity(
                buildCompletedMatchWithDetails(UUID.randomUUID(), buildCampaign("C1"), influencer));
        ChatEntity chatNew = buildChatEntity(
                buildCompletedMatchWithDetails(UUID.randomUUID(), buildCampaign("C2"), influencer));

        LocalDateTime older = LocalDateTime.now().minusHours(2);
        LocalDateTime newer = LocalDateTime.now();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(chatRepository.findAllByUserId(userId)).thenReturn(List.of(chatOld, chatNew));
        when(messageRepository.findTopByChat_IdOrderBySentAtDesc(chatOld.getId()))
                .thenReturn(Optional.of(buildMessage(chatOld, "Antiguo", older)));
        when(messageRepository.findTopByChat_IdOrderBySentAtDesc(chatNew.getId()))
                .thenReturn(Optional.of(buildMessage(chatNew, "Reciente", newer)));

        List<Chat> result = chatPersistenceJpa.findAllByUser("user@test.com");

        assertEquals("Reciente", result.get(0).getLastMessage());
        assertEquals("Antiguo",  result.get(1).getLastMessage());
    }

    @Test
    void findAllByUser_shouldPopulateCampaignTitle() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, "user@test.com");
        InfluencerEntity influencer = buildInfluencer("Ana");
        influencer.setId(userId);
        CampaignEntity campaign = buildCampaign("Campaña Verano 2025");
        MatchEntity match = buildCompletedMatchWithDetails(UUID.randomUUID(), campaign, influencer);
        ChatEntity chatEntity = buildChatEntity(match);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(chatRepository.findAllByUserId(userId)).thenReturn(List.of(chatEntity));
        when(messageRepository.findTopByChat_IdOrderBySentAtDesc(any())).thenReturn(Optional.empty());

        List<Chat> result = chatPersistenceJpa.findAllByUser("user@test.com");

        assertEquals("Campaña Verano 2025", result.get(0).getCampaignTitle());
    }

    @Test
    void findAllByUser_shouldLeaveCampaignTitleNull_whenCampaignIsNull() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, "user@test.com");
        InfluencerEntity influencer = buildInfluencer("Ana");
        influencer.setId(userId);
        MatchEntity match = buildCompletedMatch(UUID.randomUUID());
        match.setInfluencer(influencer);
        match.setCampaign(null);
        ChatEntity chatEntity = buildChatEntity(match);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(chatRepository.findAllByUserId(userId)).thenReturn(List.of(chatEntity));
        when(messageRepository.findTopByChat_IdOrderBySentAtDesc(any())).thenReturn(Optional.empty());

        List<Chat> result = chatPersistenceJpa.findAllByUser("user@test.com");

        assertNull(result.get(0).getCampaignTitle());
    }

    @Test
    void findAllByUser_shouldDistinguishChatsFromSameBusinessByTitle() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, "influencer@test.com");
        InfluencerEntity influencer = buildInfluencer("Ana");
        influencer.setId(userId);
        BusinessEntity business = buildBusiness("Nike Spain");

        CampaignEntity c1 = buildCampaign("Campaña Verano");
        c1.setBusiness(business);
        CampaignEntity c2 = buildCampaign("Colección Otoño");
        c2.setBusiness(business);

        ChatEntity chat1 = buildChatEntity(buildCompletedMatchWithDetails(UUID.randomUUID(), c1, influencer));
        ChatEntity chat2 = buildChatEntity(buildCompletedMatchWithDetails(UUID.randomUUID(), c2, influencer));

        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(user));
        when(chatRepository.findAllByUserId(userId)).thenReturn(List.of(chat1, chat2));
        when(messageRepository.findTopByChat_IdOrderBySentAtDesc(any())).thenReturn(Optional.empty());

        List<Chat> result = chatPersistenceJpa.findAllByUser("influencer@test.com");

        assertEquals(2, result.size());
        assertEquals("Nike Spain", result.get(0).getDisplayName());
        assertEquals("Nike Spain", result.get(1).getDisplayName());
        // Las campañas deben ser distintas para poder diferenciarlos
        assertNotEquals(result.get(0).getCampaignTitle(), result.get(1).getCampaignTitle());
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

    private MessageEntity buildMessage(ChatEntity chat, String text, LocalDateTime sentAt) {
        MessageEntity m = new MessageEntity();
        m.setId(UUID.randomUUID());
        m.setText(text);
        m.setSentAt(sentAt);
        m.setIdUser(UUID.randomUUID());
        m.setChat(chat);
        return m;
    }

    private UserEntity buildUser(UUID id, String email) {
        UserEntity u = new UserEntity();
        u.setId(id);
        u.setEmail(email);
        return u;
    }

    private BusinessEntity buildBusiness(String name) {
        BusinessEntity b = new BusinessEntity();
        b.setId(UUID.randomUUID());
        b.setName(name);
        return b;
    }
}
