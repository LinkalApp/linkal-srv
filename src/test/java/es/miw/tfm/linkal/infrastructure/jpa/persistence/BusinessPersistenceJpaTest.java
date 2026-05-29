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

import java.util.Optional;
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

    // ---------------------------------------------------------------------------
    //  readMe
    // ---------------------------------------------------------------------------

    @Test
    void readMe_shouldReturnBusinessWhenFound() {
        BusinessEntity entity = buildBusinessEntity(buildBusiness());

        when(businessRepository.findByEmail("empresa@test.com")).thenReturn(Optional.of(entity));

        Business result = businessPersistenceJpa.readMe("empresa@test.com");

        assertEquals("empresa@test.com", result.getEmail());
        assertEquals("Calle Mayor 1", result.getAddress());
        assertEquals("Madrid", result.getProvince());
        assertEquals("https://miempresa.com", result.getWebsite());
        assertEquals("Moda", result.getCategory());
    }

    @Test
    void readMe_shouldThrowNotFoundExceptionWhenEmailNotFound() {
        when(businessRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> businessPersistenceJpa.readMe("unknown@test.com"));
    }

    @Test
    void readMe_shouldDelegateToRepositoryFindByEmail() {
        BusinessEntity entity = buildBusinessEntity(buildBusiness());

        when(businessRepository.findByEmail("empresa@test.com")).thenReturn(Optional.of(entity));

        businessPersistenceJpa.readMe("empresa@test.com");

        verify(businessRepository).findByEmail("empresa@test.com");
    }

    // ------------------------------------------------------------------------
    //  updateMe
    // ------------------------------------------------------------------------

    @Test
    void updateMe_shouldUpdateFieldsAndSave() {
        BusinessEntity existing = buildBusinessEntity(buildBusiness());

        Business patch = new Business();
        patch.setName("Nuevo Nombre");
        patch.setAddress("Avenida Nueva 10");
        patch.setWebsite("https://nuevaweb.com");

        when(businessRepository.findByEmail("empresa@test.com")).thenReturn(Optional.of(existing));
        when(businessRepository.save(existing)).thenReturn(existing);

        businessPersistenceJpa.updateMe("empresa@test.com", patch);

        assertEquals("Nuevo Nombre", existing.getName());
        assertEquals("Avenida Nueva 10", existing.getAddress());
        assertEquals("https://nuevaweb.com", existing.getWebsite());
        verify(businessRepository).save(existing);
    }

    @Test
    void updateMe_shouldNotOverwriteFieldsWhenPatchValueIsNull() {
        BusinessEntity existing = buildBusinessEntity(buildBusiness());

        Business patch = new Business(); // todos null

        when(businessRepository.findByEmail("empresa@test.com")).thenReturn(Optional.of(existing));
        when(businessRepository.save(existing)).thenReturn(existing);

        businessPersistenceJpa.updateMe("empresa@test.com", patch);

        // Campos originales intactos
        assertEquals("Calle Mayor 1", existing.getAddress());
        assertEquals("https://miempresa.com", existing.getWebsite());
        assertEquals("Madrid", existing.getProvince());
    }

    @Test
    void updateMe_shouldThrowNotFoundExceptionWhenEmailNotFound() {
        when(businessRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> businessPersistenceJpa.updateMe("unknown@test.com", new Business()));
        verify(businessRepository, never()).save(any());
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
