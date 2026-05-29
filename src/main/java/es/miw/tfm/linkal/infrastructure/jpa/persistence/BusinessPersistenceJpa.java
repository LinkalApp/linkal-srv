package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Business;
import es.miw.tfm.linkal.domain.persistence.BusinessPersistence;
import es.miw.tfm.linkal.infrastructure.jpa.entities.BusinessEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.InfluencerEntity;
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

    @Override
    public Business readMe(String email) {
        return businessRepository.findByEmail(email)
                .map(BusinessEntity::toBusiness)
                .orElseThrow(() -> new NotFoundException("Business not found: " + email));
    }

    @Override
    public Business updateMe(String email, Business business) {
        BusinessEntity entity = businessRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Business not found: " + email));
        applyUpdates(entity, business);
        return businessRepository.save(entity).toBusiness();
    }

    @Override
    public void deleteMe(String email) {
        BusinessEntity entity = businessRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Business not found: " + email));
        businessRepository.delete(entity);
    }

    private void applyUpdates(BusinessEntity entity, Business business) {
        if (business.getName() != null) entity.setName(business.getName());
        if (business.getPhoneNumber() != null) entity.setPhoneNumber(business.getPhoneNumber());
        if (business.getDescription() != null) entity.setDescription(business.getDescription());
        if (business.getAddress() != null) entity.setAddress(business.getAddress());
        if (business.getProvince() != null) entity.setProvince(business.getProvince());
        if (business.getWebsite() != null) entity.setWebsite(business.getWebsite());
        if (business.getCategory() != null) entity.setCategory(business.getCategory());
    }
}
