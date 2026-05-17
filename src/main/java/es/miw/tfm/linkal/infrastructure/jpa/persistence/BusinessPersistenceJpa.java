package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.model.Business;
import es.miw.tfm.linkal.domain.persistence.BusinessPersistence;
import es.miw.tfm.linkal.infrastructure.jpa.entities.BusinessEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.BusinessRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BusinessPersistenceJpa implements BusinessPersistence {
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    @Override
    public Business create(Business business) {
        if (userRepository.existsByEmail(business.getEmail())) {
            throw new ConflictException("Email already registered: " + business.getEmail());
        }
        return businessRepository.save(new BusinessEntity(business)).toBusiness();
    }
}
