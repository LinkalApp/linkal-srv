package es.miw.tfm.linkal.domain.model;
import com.fasterxml.jackson.annotation.JsonInclude;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private UUID id;
    private String name;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    private String phoneNumber;
    private String description;
    private Boolean verified;
    private RoleType role;
}