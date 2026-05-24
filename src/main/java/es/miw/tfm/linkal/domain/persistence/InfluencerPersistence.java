package es.miw.tfm.linkal.domain.persistence;

import es.miw.tfm.linkal.domain.model.Influencer;
import org.springframework.stereotype.Repository;

@Repository
public interface InfluencerPersistence {
    Influencer create(Influencer influencer);
    Influencer readMe(String email);
    Influencer updateMe (String email, Influencer influencer);
    void deleteMe(String email);
}
