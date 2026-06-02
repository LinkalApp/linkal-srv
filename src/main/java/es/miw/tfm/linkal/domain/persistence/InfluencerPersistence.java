package es.miw.tfm.linkal.domain.persistence;

import es.miw.tfm.linkal.domain.model.Influencer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfluencerPersistence {
    Influencer create(Influencer influencer);
    Influencer readMe(String email);
    List<Influencer> readAll();
    Influencer updateMe (String email, Influencer influencer);
    void deleteMe(String email);
}
