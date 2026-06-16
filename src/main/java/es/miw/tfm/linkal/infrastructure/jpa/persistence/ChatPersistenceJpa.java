package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.BadRequestException;
import es.miw.tfm.linkal.domain.exceptions.ForbiddenException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Chat;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import es.miw.tfm.linkal.domain.persistence.ChatPersistence;
import es.miw.tfm.linkal.infrastructure.jpa.entities.ChatEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.MatchEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.UserEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.ChatRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.MatchRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ChatPersistenceJpa implements ChatPersistence {

    private final ChatRepository chatRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

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

    private String buildChatName(MatchEntity match) {
        String campaign   = match.getCampaign()   != null ? match.getCampaign().getTitle()   : "Campaña";
        String influencer = match.getInfluencer() != null ? match.getInfluencer().getName()  : "Influencer";
        return campaign + " · " + influencer;
    }
}
