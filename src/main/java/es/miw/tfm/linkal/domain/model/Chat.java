package es.miw.tfm.linkal.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Chat {
    private UUID id;
    private String name;
    private UUID matchId;
    private UUID campaignId;

    // Campos para el listado
    private String displayName;
    private String campaignTitle;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}
