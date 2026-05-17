package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Business;
import es.miw.tfm.linkal.domain.persistence.BusinessPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessService {
    private final BusinessPersistence businessPersistence;
    private final PasswordEncoder passwordEncoder;

    public Business create(Business business) {
        business.setPassword(passwordEncoder.encode(business.getPassword()));
        business.setVerified(false);
        return businessPersistence.create(business);
    }
}
