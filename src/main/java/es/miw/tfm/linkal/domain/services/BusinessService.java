package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Business;
import es.miw.tfm.linkal.domain.persistence.BusinessPersistence;
import es.miw.tfm.linkal.domain.persistence.EvaluationPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessService {
    private final BusinessPersistence businessPersistence;
    private final EvaluationPersistence evaluationPersistence;
    private final PasswordEncoder passwordEncoder;

    public Business create(Business business) {
        business.setPassword(passwordEncoder.encode(business.getPassword()));
        business.setVerified(false);
        return businessPersistence.create(business);
    }

    public Business readMe(String email) {
        Business business = businessPersistence.readMe(email);
        business.setPassword(null);
        business.setAverageRating(evaluationPersistence.averageScoreByBusinessId(business.getId()));
        return business;
    }

    public Business updateMe(String email, Business business) {
        Business updated = businessPersistence.updateMe(email, business);
        updated.setPassword(null);
        updated.setAverageRating(evaluationPersistence.averageScoreByBusinessId(updated.getId()));
        return updated;
    }
}
