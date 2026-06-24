package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.AdminUserDetail;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import es.miw.tfm.linkal.domain.persistence.UserPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserPersistence userPersistence;

    public List<AdminUserDetail> findAll(RoleType role, Boolean verified) {
        return userPersistence.findAll(role, verified);
    }

    public AdminUserDetail findById(UUID id) {
        return userPersistence.findById(id);
    }
}
