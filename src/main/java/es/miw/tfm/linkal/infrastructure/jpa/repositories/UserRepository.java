package es.miw.tfm.linkal.infrastructure.jpa.repositories;

import es.miw.tfm.linkal.domain.model.enums.RoleType;
import es.miw.tfm.linkal.infrastructure.jpa.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);
    Optional<UserEntity> findByEmail(String email);
    @Query("SELECT u FROM UserEntity u WHERE " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:verified IS NULL OR u.verified = :verified)")
    List<UserEntity> findAllFiltered(@Param("role") RoleType role,
                                     @Param("verified") Boolean verified);
}
