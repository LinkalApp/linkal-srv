package es.miw.tfm.linkal.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.miw.tfm.linkal.domain.model.enums.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Campaign {
    private UUID id;
    private String title;
    private String description;
    private String requirements;
    private Double reward;
    private LocalDate creationDate;
    private String objective;
    private CampaignStatus status;
    private UUID businessId;
}
