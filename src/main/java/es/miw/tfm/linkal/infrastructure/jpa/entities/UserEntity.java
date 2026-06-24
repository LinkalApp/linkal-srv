package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.User;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role_type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("ADMIN")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(length = 1000)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean verified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", insertable = false, updatable = false)
    private RoleType role;

    public UserEntity(User user) {
        BeanUtils.copyProperties(user, this);
    }

    public User toUser() {
        User user = new User();
        BeanUtils.copyProperties(this, user);
        return user;
    }
}