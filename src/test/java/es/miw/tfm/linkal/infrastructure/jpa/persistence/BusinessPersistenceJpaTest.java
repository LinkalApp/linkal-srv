package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Business;
import es.miw.tfm.linkal.infrastructure.jpa.entities.BusinessEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.BusinessRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BusinessPersistenceJpaTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BusinessPersistenceJpa businessPersistenceJpa;

    // -------------------------------------------------------------------------
    //  create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldSaveAndReturnBusiness() {
        Business business = buildBusiness();
        BusinessEntity savedEntity = buildBusinessEntity(business);

        when(userRepository.existsByEmail("empresa@test.com")).thenReturn(false);
        when(businessRepository.save(any(BusinessEntity.class))).thenReturn(savedEntity);

        Business result = businessPersistenceJpa.create(business);

        assertNotNull(result);
        assertEquals("empresa@test.com", result.getEmail());
        assertEquals("Calle Mayor 1", result.getAddress());
        assertEquals("https://miempresa.com", result.getWebsite());
        verify(businessRepository).save(any(BusinessEntity.class));
    }

    @Test
    void create_shouldThrowConflictExceptionWhenEmailAlreadyExists() {
        Business business = buildBusiness();

        when(userRepository.existsByEmail("empresa@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> businessPersistenceJpa.create(business));
        verify(businessRepository, never()).save(any());
    }

    @Test
    void create_shouldCheckEmailBeforeSaving() {
        Business business = buildBusiness();
        BusinessEntity savedEntity = buildBusinessEntity(business);

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(businessRepository.save(any())).thenReturn(savedEntity);

        businessPersistenceJpa.create(business);

        var inOrder = inOrder(userRepository, businessRepository);
        inOrder.verify(userRepository).existsByEmail("empresa@test.com");
        inOrder.verify(businessRepository).save(any());
    }

    // -------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    private Business buildBusiness() {
        Business business = new Business();
        business.setName("Mi Empresa");
        business.setEmail("empresa@test.com");
        business.setPassword("hashedPass");
        business.setAddress("Calle Mayor 1");
        business.setProvince("Madrid");
        business.setWebsite("https://miempresa.com");
        business.setCategory("Moda");
        business.setVerified(false);
        return business;
    }

    private BusinessEntity buildBusinessEntity(Business business) {
        BusinessEntity entity = new BusinessEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(business.getName());
        entity.setEmail(business.getEmail());
        entity.setPassword(business.getPassword());
        entity.setAddress(business.getAddress());
        entity.setProvince(business.getProvince());
        entity.setWebsite(business.getWebsite());
        entity.setCategory(business.getCategory());
        entity.setVerified(false);
        return entity;
    }
}
