package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.model.Influencer;
import es.miw.tfm.linkal.domain.persistence.InfluencerPersistence;
import es.miw.tfm.linkal.infrastructure.jpa.entities.InfluencerEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.InfluencerRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InfluencerPersistenceJpa implements InfluencerPersistence {
    private final InfluencerRepository influencerRepository;
    private final UserRepository userRepository;

    @Override
    public Influencer create(Influencer influencer) {
        if (userRepository.existsByEmail(influencer.getEmail())) {
            throw new ConflictException("Email already registered: " + influencer.getEmail());
        }
        return influencerRepository.save(new InfluencerEntity(influencer)).toInfluencer();
    }
}
