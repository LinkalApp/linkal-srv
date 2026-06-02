package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Influencer;
import es.miw.tfm.linkal.domain.persistence.InfluencerPersistence;
import es.miw.tfm.linkal.infrastructure.jpa.entities.InfluencerEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.InfluencerRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public Influencer readMe(String email) {
        return influencerRepository.findByEmail(email)
                .map(InfluencerEntity::toInfluencer)
                .orElseThrow(() -> new NotFoundException("Influencer not found: " + email));
    }

    @Override
    public List<Influencer> readAll() {
        return influencerRepository.findAll().stream()
                .map(InfluencerEntity::toInfluencer)
                .toList();
    }

    @Override
    public Influencer updateMe(String email, Influencer influencer) {
        InfluencerEntity entity = influencerRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Influencer not found: " + email));
        applyUpdates(entity, influencer);

        boolean hasInstagram = entity.getInstagram() != null && !entity.getInstagram().isEmpty();
        boolean hasTiktok    = entity.getTiktok()    != null && !entity.getTiktok().isEmpty();
        boolean hasYoutube   = entity.getYoutube()   != null && !entity.getYoutube().isEmpty();
        if (!hasInstagram && !hasTiktok && !hasYoutube) {
            throw new ConflictException("At least one social network (Instagram, TikTok or YouTube) must be set");
        }

        return influencerRepository.save(entity).toInfluencer();
    }

    @Override
    public void deleteMe(String email) {
        InfluencerEntity entity = influencerRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Influencer not found: " + email));
        influencerRepository.delete(entity);
    }

    private void applyUpdates(InfluencerEntity entity, Influencer influencer) {
        if (influencer.getName()          != null) entity.setName(influencer.getName());
        if (influencer.getPhoneNumber()   != null) entity.setPhoneNumber(influencer.getPhoneNumber());
        if (influencer.getDescription()   != null) entity.setDescription(influencer.getDescription());
        if (influencer.getArtisticName()  != null) entity.setArtisticName(influencer.getArtisticName());
        if (influencer.getInterests()     != null) entity.setInterests(influencer.getInterests());
        if (influencer.getInstagram()     != null) entity.setInstagram(influencer.getInstagram());
        if (influencer.getTiktok()        != null) entity.setTiktok(influencer.getTiktok());
        if (influencer.getYoutube()       != null) entity.setYoutube(influencer.getYoutube());
    }
}
