package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.BadRequestException;
import es.miw.tfm.linkal.domain.exceptions.ForbiddenException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Chat;
import es.miw.tfm.linkal.domain.model.Message;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import es.miw.tfm.linkal.domain.persistence.ChatPersistence;
import es.miw.tfm.linkal.infrastructure.jpa.entities.*;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.ChatRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.MatchRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.MessageRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ChatPersistenceJpa implements ChatPersistence {

    private final ChatRepository chatRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public Chat createByMatch(UUID matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found: " + matchId));

        if (match.getStatus() != MatchStatus.COMPLETED) {
            throw new BadRequestException("No se puede crear un chat sin un Match COMPLETED asociado");
        }

        return chatRepository.findByMatch_Id(matchId)
                .map(existing -> {
                    return existing.toChat();
                })
                .orElseGet(() -> {
                    String name = buildChatName(match);
                    ChatEntity entity = ChatEntity.builder()
                            .match(match)
                            .campaign(match.getCampaign())
                            .name(name)
                            .build();
                    return chatRepository.save(entity).toChat();
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> findAllByUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        return chatRepository.findAllByUserId(user.getId())
                .stream()
                .map(c -> {
                    Chat chat = c.toChat();
                    chat.setDisplayName(resolveDisplayName(c, user.getId()));
                    if (c.getMatch().getCampaign() != null) {
                        chat.setCampaignTitle(c.getMatch().getCampaign().getTitle());
                    }
                    messageRepository.findTopByChat_IdOrderBySentAtDesc(c.getId())
                            .ifPresent(msg -> {
                                chat.setLastMessage(msg.getText());
                                chat.setLastMessageAt(msg.getSentAt());
                            });
                    return chat;
                })
                .sorted(Comparator.comparing(
                        chat -> chat.getLastMessageAt() != null ? chat.getLastMessageAt() : LocalDateTime.MIN,
                        Comparator.reverseOrder()))
                .toList();
    }

    @Override
    @Transactional
    public Message sendMessage(UUID chatId, String text, String senderEmail) {
        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found: " + chatId));

        UserEntity sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new NotFoundException("User not found: " + senderEmail));

        assertUserBelongsToChat(sender.getId(), chat);

        MessageEntity msg = MessageEntity.builder()
                .text(text)
                .sentAt(LocalDateTime.now())
                .senderId(sender.getId())
                .chat(chat)
                .build();

        return messageRepository.save(msg).toMessage();
    }

    // Helpers ------------------------------------------------------------------------------------------

    private String buildChatName(MatchEntity match) {
        String campaign   = match.getCampaign()   != null ? match.getCampaign().getTitle()   : "Campaña";
        String influencer = match.getInfluencer() != null ? match.getInfluencer().getName()  : "Influencer";
        return campaign + " · " + influencer;
    }

    private String resolveDisplayName(ChatEntity chat, UUID userId) {
        InfluencerEntity influencer = chat.getMatch().getInfluencer();
        boolean isInfluencer = influencer != null && userId.equals(influencer.getId());
        if (isInfluencer) {
            var business = chat.getMatch().getCampaign() != null
                    ? chat.getMatch().getCampaign().getBusiness() : null;
            return business != null ? business.getName() : "";
        } else {
            return influencer != null ? influencer.getName() : "";
        }
    }

    private void assertUserBelongsToChat(UUID userId, ChatEntity chat) {
        MatchEntity match = chat.getMatch();
        InfluencerEntity influencer = match.getInfluencer();
        UUID businessId = match.getCampaign() != null && match.getCampaign().getBusiness() != null
                ? match.getCampaign().getBusiness().getId() : null;

        boolean isInfluencer = influencer != null && userId.equals(influencer.getId());
        boolean isBusiness   = userId.equals(businessId);

        if (!isInfluencer && !isBusiness) {
            throw new ForbiddenException("No tienes acceso a este chat");
        }
    }
}
