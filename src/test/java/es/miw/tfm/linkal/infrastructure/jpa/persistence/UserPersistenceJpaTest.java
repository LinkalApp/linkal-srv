package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.AdminUserDetail;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import es.miw.tfm.linkal.infrastructure.jpa.entities.BusinessEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.InfluencerEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserPersistenceJpaTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserPersistenceJpa userPersistenceJpa;

    // ------------------------------------------------------------------------
    //  findAll
    // ------------------------------------------------------------------------

    @Test
    void findAll_withNoFilters_returnsAllUsers() {
        InfluencerEntity influencer = buildInfluencerEntity();
        BusinessEntity business     = buildBusinessEntity();

        when(userRepository.findAllFiltered(null, null))
                .thenReturn(Arrays.asList(influencer, business));

        List<AdminUserDetail> result = userPersistenceJpa.findAll(null, null);

        assertEquals(2, result.size());
        verify(userRepository).findAllFiltered(null, null);
    }

    @Test
    void findAll_withRoleFilter_delegatesToRepository() {
        when(userRepository.findAllFiltered(RoleType.INFLUENCER, null))
                .thenReturn(List.of(buildInfluencerEntity()));

        List<AdminUserDetail> result = userPersistenceJpa.findAll(RoleType.INFLUENCER, null);

        assertEquals(1, result.size());
        verify(userRepository).findAllFiltered(RoleType.INFLUENCER, null);
    }

    @Test
    void findAll_withVerifiedFilter_delegatesToRepository() {
        when(userRepository.findAllFiltered(null, true))
                .thenReturn(List.of(buildInfluencerEntity()));

        List<AdminUserDetail> result = userPersistenceJpa.findAll(null, true);

        assertEquals(1, result.size());
        verify(userRepository).findAllFiltered(null, true);
    }

    @Test
    void findAll_withBothFilters_delegatesToRepository() {
        when(userRepository.findAllFiltered(RoleType.BUSINESS, false))
                .thenReturn(Collections.emptyList());

        List<AdminUserDetail> result = userPersistenceJpa.findAll(RoleType.BUSINESS, false);

        assertEquals(0, result.size());
        verify(userRepository).findAllFiltered(RoleType.BUSINESS, false);
    }

    @Test
    void findAll_mapsInfluencerFieldsCorrectly() {
        InfluencerEntity entity = buildInfluencerEntity();
        when(userRepository.findAllFiltered(any(), any())).thenReturn(List.of(entity));

        List<AdminUserDetail> result = userPersistenceJpa.findAll(null, null);

        AdminUserDetail detail = result.get(0);
        assertEquals("Test Influencer", detail.getName());
        assertEquals("influencer@test.com", detail.getEmail());
        assertEquals(RoleType.INFLUENCER, detail.getRole());
        assertEquals("ArtName", detail.getArtisticName());
        assertNotNull(detail.getInterests());
    }

    @Test
    void findAll_mapsBusinessFieldsCorrectly() {
        BusinessEntity entity = buildBusinessEntity();
        when(userRepository.findAllFiltered(any(), any())).thenReturn(List.of(entity));

        List<AdminUserDetail> result = userPersistenceJpa.findAll(null, null);

        AdminUserDetail detail = result.get(0);
        assertEquals("Test Business", detail.getName());
        assertEquals("business@test.com", detail.getEmail());
        assertEquals(RoleType.BUSINESS, detail.getRole());
        assertEquals("Moda", detail.getCategory());
        assertEquals("Madrid", detail.getProvince());
    }

    @Test
    void findAll_influencerHasNoBusinessFields() {
        InfluencerEntity entity = buildInfluencerEntity();
        when(userRepository.findAllFiltered(any(), any())).thenReturn(List.of(entity));

        AdminUserDetail detail = userPersistenceJpa.findAll(null, null).get(0);

        assertNull(detail.getCategory());
        assertNull(detail.getProvince());
        assertNull(detail.getAddress());
        assertNull(detail.getWebsite());
    }

    @Test
    void findAll_businessHasNoInfluencerFields() {
        BusinessEntity entity = buildBusinessEntity();
        when(userRepository.findAllFiltered(any(), any())).thenReturn(List.of(entity));

        AdminUserDetail detail = userPersistenceJpa.findAll(null, null).get(0);

        assertNull(detail.getArtisticName());
        assertNull(detail.getInstagram());
        assertNull(detail.getTiktok());
        assertNull(detail.getYoutube());
    }

    @Test
    void findAll_whenEmpty_returnsEmptyList() {
        when(userRepository.findAllFiltered(any(), any())).thenReturn(Collections.emptyList());

        List<AdminUserDetail> result = userPersistenceJpa.findAll(null, null);

        assertTrue(result.isEmpty());
    }

    // ------------------------------------------------------------------------
    //  findById
    // ------------------------------------------------------------------------

    @Test
    void findById_returnsInfluencerDetail() {
        UUID id = UUID.randomUUID();
        InfluencerEntity entity = buildInfluencerEntity();
        entity.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(entity));

        AdminUserDetail result = userPersistenceJpa.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(RoleType.INFLUENCER, result.getRole());
        assertEquals("ArtName", result.getArtisticName());
    }

    @Test
    void findById_returnsBusinessDetail() {
        UUID id = UUID.randomUUID();
        BusinessEntity entity = buildBusinessEntity();
        entity.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(entity));

        AdminUserDetail result = userPersistenceJpa.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(RoleType.BUSINESS, result.getRole());
        assertEquals("Moda", result.getCategory());
    }

    @Test
    void findById_throwsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userPersistenceJpa.findById(id));
    }

    // ---------------------------------------------------------------------------
    //  helpers
    // ---------------------------------------------------------------------------

    private InfluencerEntity buildInfluencerEntity() {
        InfluencerEntity entity = new InfluencerEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Test Influencer");
        entity.setEmail("influencer@test.com");
        entity.setPassword("hashed");
        entity.setVerified(true);
        entity.setRole(RoleType.INFLUENCER);
        entity.setArtisticName("ArtName");
        entity.setInterests(Arrays.asList("Moda", "Viajes"));
        entity.setInstagram("@test");
        entity.setTiktok(null);
        entity.setYoutube(null);
        entity.setMatches(new java.util.ArrayList<>());
        return entity;
    }

    private BusinessEntity buildBusinessEntity() {
        BusinessEntity entity = new BusinessEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Test Business");
        entity.setEmail("business@test.com");
        entity.setPassword("hashed");
        entity.setVerified(false);
        entity.setRole(RoleType.BUSINESS);
        entity.setCategory("Moda");
        entity.setProvince("Madrid");
        entity.setAddress("Calle Mayor 1");
        entity.setWebsite("https://test.com");
        entity.setCampaigns(new java.util.ArrayList<>());
        return entity;
    }
}

