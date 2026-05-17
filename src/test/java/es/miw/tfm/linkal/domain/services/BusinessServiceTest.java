package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Business;
import es.miw.tfm.linkal.domain.persistence.BusinessPersistence;
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
