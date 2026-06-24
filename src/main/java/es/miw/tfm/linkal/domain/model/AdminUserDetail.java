package es.miw.tfm.linkal.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminUserDetail {
    // Campos comunes
    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private String description;
    private Boolean verified;
    private RoleType role;

    // Campos exclusivos de Influencer
    private String artisticName;
    private List<String> interests;
    private String instagram;
    private String tiktok;
    private String youtube;

    // Campos exclusivos de Business
    private String address;
    private String province;
    private String website;
    private String category;
}
