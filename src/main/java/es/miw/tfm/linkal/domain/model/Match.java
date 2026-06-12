package es.miw.tfm.linkal.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Match {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime matchedAt;
    private MatchStatus status;
    private UUID campaignId;
    private UUID influencerId;
    private UUID businessId;

    // Campos enriquecidos — campaña
    private String campaignTitle;
    private String campaignDescription;
    private String campaignObjective;
    private String campaignRequirements;
    private String campaignReward;
    private String campaignStatus;
    private String campaignCreationDate;
    // Campos enriquecidos — negocio
    private String businessName;
    private String businessCategory;
    private String businessDescription;
    private String businessWebsite;
    private String businessProvince;
    private String businessAddress;
    private Boolean businessVerified;
    // Campos enriquecidos — influencer
    private String       influencerName;
    private String       influencerArtisticName;
    private String       influencerDescription;
    private String       influencerEmail;
    private String       influencerInstagram;
    private String       influencerTiktok;
    private String       influencerYoutube;
    private Boolean      influencerVerified;
    private List<String> influencerInterests;
}
