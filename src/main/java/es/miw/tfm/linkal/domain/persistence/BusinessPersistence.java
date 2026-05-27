package es.miw.tfm.linkal.domain.persistence;

import es.miw.tfm.linkal.domain.model.Business;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessPersistence {
    Business create(Business business);
    Business readMe(String email);
}
