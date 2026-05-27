package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Business;
import es.miw.tfm.linkal.domain.persistence.BusinessPersistence;
import es.miw.tfm.linkal.domain.persistence.EvaluationPersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BusinessServiceTest {
    @Mock
    private BusinessPersistence businessPersistence;
    @Mock
    private EvaluationPersistence evaluationPersistence;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BusinessService businessService;

    // -------------------------------------------------------------------------
    //  create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldEncodePassword() {
        Business business = buildBusiness("rawPass");

        when(passwordEncoder.encode("rawPass")).thenReturn("hashedPass");
        when(businessPersistence.create(any())).thenReturn(business);

        businessService.create(business);

        verify(passwordEncoder).encode("rawPass");
        assertEquals("hashedPass", business.getPassword());
    }

    @Test
    void create_shouldSetVerifiedToFalse() {
        Business business = buildBusiness("rawPass");

        when(passwordEncoder.encode(any())).thenReturn("hashedPass");
        when(businessPersistence.create(any())).thenReturn(business);

        businessService.create(business);

        assertFalse(business.getVerified());
    }

    @Test
    void create_shouldDelegateToPersistence() {
        Business business = buildBusiness("rawPass");
        Business saved = buildBusiness("hashedPass");
        saved.setId(UUID.randomUUID());

        when(passwordEncoder.encode(any())).thenReturn("hashedPass");
        when(businessPersistence.create(any())).thenReturn(saved);

        Business result = businessService.create(business);

        assertNotNull(result.getId());
        verify(businessPersistence).create(business);
    }

    @Test
    void create_shouldPreserveBusinessFields() {
        Business business = buildBusiness("rawPass");
        business.setAddress("Calle Mayor 1");
        business.setWebsite("https://miempresa.com");
        business.setProvince("Madrid");
        business.setCategory("Moda");

        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(businessPersistence.create(any())).thenReturn(business);

        businessService.create(business);

        assertEquals("Calle Mayor 1", business.getAddress());
        assertEquals("https://miempresa.com", business.getWebsite());
        assertEquals("Madrid", business.getProvince());
        assertEquals("Moda", business.getCategory());
    }

    // ------------------------------------------------------------------------
    //  readMe
    // -------------------------------------------------------------------------

    @Test
    void readMe_shouldReturnBusinessWithNullPassword() {
        Business business = buildBusiness("hashedPass");
        business.setId(UUID.randomUUID());

        when(businessPersistence.readMe("business@test.com")).thenReturn(business);
        when(evaluationPersistence.averageScoreByBusinessId(business.getId())).thenReturn(null);

        Business result = businessService.readMe("business@test.com");

        assertNull(result.getPassword());
        assertEquals("business@test.com", result.getEmail());
    }

    @Test
    void readMe_shouldDelegateToPersistence() {
        Business business = buildBusiness("hashedPass");
        business.setId(UUID.randomUUID());

        when(businessPersistence.readMe("business@test.com")).thenReturn(business);
        when(evaluationPersistence.averageScoreByBusinessId(business.getId())).thenReturn(null);

        businessService.readMe("business@test.com");

        verify(businessPersistence).readMe("business@test.com");
    }

    @Test
    void readMe_shouldReturnBusinessFieldsIntact() {
        Business business = buildBusiness("hashedPass");
        business.setId(UUID.randomUUID());
        business.setAddress("Calle Mayor 1");
        business.setProvince("Madrid");
        business.setWebsite("https://miempresa.com");
        business.setCategory("Moda");

        when(businessPersistence.readMe("business@test.com")).thenReturn(business);
        when(evaluationPersistence.averageScoreByBusinessId(business.getId())).thenReturn(null);

        Business result = businessService.readMe("business@test.com");

        assertEquals("Calle Mayor 1", result.getAddress());
        assertEquals("Madrid", result.getProvince());
        assertEquals("https://miempresa.com", result.getWebsite());
        assertEquals("Moda", result.getCategory());
    }

    @Test
    void readMe_shouldSetAverageRating() {
        UUID id = UUID.randomUUID();
        Business business = buildBusiness("hashedPass");
        business.setId(id);

        when(businessPersistence.readMe("business@test.com")).thenReturn(business);
        when(evaluationPersistence.averageScoreByBusinessId(id)).thenReturn(4.2);

        Business result = businessService.readMe("business@test.com");

        assertEquals(4.2, result.getAverageRating());
        verify(evaluationPersistence).averageScoreByBusinessId(id);
    }

    @Test
    void readMe_shouldSetAverageRatingNullWhenNoEvaluations() {
        UUID id = UUID.randomUUID();
        Business business = buildBusiness("hashedPass");
        business.setId(id);

        when(businessPersistence.readMe("business@test.com")).thenReturn(business);
        when(evaluationPersistence.averageScoreByBusinessId(id)).thenReturn(null);

        Business result = businessService.readMe("business@test.com");

        assertNull(result.getAverageRating());
    }

    // -------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    private Business buildBusiness(String password) {
        Business business = new Business();
        business.setName("Test Business");
        business.setEmail("business@test.com");
        business.setPassword(password);
        return business;
    }
}
