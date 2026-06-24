package es.miw.tfm.linkal.domain.persistence;

import es.miw.tfm.linkal.domain.model.AdminUserDetail;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserPersistence {
    List<AdminUserDetail> findAll(RoleType role, Boolean verified);
    AdminUserDetail findById(UUID id);
}
