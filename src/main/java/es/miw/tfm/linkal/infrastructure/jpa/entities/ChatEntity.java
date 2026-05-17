package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.Chat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chats")
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignEntity campaign;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MessageEntity> messages = new ArrayList<>();

    public Chat toChat() {
        Chat chat = new Chat();
        BeanUtils.copyProperties(this, chat);
        if (this.match != null) {
            chat.setMatchId(this.match.getId());
        }
        if (this.campaign != null) {
            chat.setCampaignId(this.campaign.getId());
        }
        return chat;
    }
}