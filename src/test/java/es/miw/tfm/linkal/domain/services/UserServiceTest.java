package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.AdminUserDetail;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import es.miw.tfm.linkal.domain.persistence.UserPersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserPersistence userPersistence;

    @InjectMocks
    private UserService userService;

    // -------------------------------------------------------------------------
    //  findAll
    // -------------------------------------------------------------------------

    @Test
    void findAll_shouldDelegateToPersistence() {
        when(userPersistence.findAll(null, null)).thenReturn(Collections.emptyList());

        userService.findAll(null, null);

        verify(userPersistence).findAll(null, null);
    }

    @Test
    void findAll_shouldReturnListFromPersistence() {
        List<AdminUserDetail> expected = Arrays.asList(
                buildDetail(RoleType.INFLUENCER),
                buildDetail(RoleType.BUSINESS));
        when(userPersistence.findAll(null, null)).thenReturn(expected);

        List<AdminUserDetail> result = userService.findAll(null, null);

        assertEquals(2, result.size());
    }

    @Test
    void findAll_withRoleFilter_passesRoleToPersistence() {
        when(userPersistence.findAll(RoleType.INFLUENCER, null)).thenReturn(Collections.emptyList());

        userService.findAll(RoleType.INFLUENCER, null);

        verify(userPersistence).findAll(RoleType.INFLUENCER, null);
    }

    @Test
    void findAll_withVerifiedFilter_passesVerifiedToPersistence() {
        when(userPersistence.findAll(null, true)).thenReturn(Collections.emptyList());

        userService.findAll(null, true);

        verify(userPersistence).findAll(null, true);
    }

    @Test
    void findAll_withBothFilters_passesBothToPersistence() {
        when(userPersistence.findAll(RoleType.BUSINESS, false)).thenReturn(Collections.emptyList());

        userService.findAll(RoleType.BUSINESS, false);

        verify(userPersistence).findAll(RoleType.BUSINESS, false);
    }

    @Test
    void findAll_whenEmpty_returnsEmptyList() {
        when(userPersistence.findAll(any(), any())).thenReturn(Collections.emptyList());

        List<AdminUserDetail> result = userService.findAll(null, null);

        assertTrue(result.isEmpty());
    }

    // --------------------------------------------------------------------------
    //  findById
    // ---------------------------------------------------------------------------

    @Test
    void findById_shouldDelegateToPersistence() {
        UUID id = UUID.randomUUID();
        when(userPersistence.findById(id)).thenReturn(buildDetail(RoleType.INFLUENCER));

        userService.findById(id);

        verify(userPersistence).findById(id);
    }

    @Test
    void findById_shouldReturnDetailFromPersistence() {
        UUID id = UUID.randomUUID();
        AdminUserDetail expected = buildDetail(RoleType.INFLUENCER);
        when(userPersistence.findById(id)).thenReturn(expected);

        AdminUserDetail result = userService.findById(id);

        assertNotNull(result);
        assertEquals(RoleType.INFLUENCER, result.getRole());
    }

    @Test
    void findById_shouldNotCallFindAll() {
        UUID id = UUID.randomUUID();
        when(userPersistence.findById(id)).thenReturn(buildDetail(RoleType.BUSINESS));

        userService.findById(id);

        verify(userPersistence, never()).findAll(any(), any());
    }

    // -------------------------------------------------------------------------
    //  updateVerified
    // -------------------------------------------------------------------------

    @Test
    void updateVerified_shouldDelegateToPersistence() {
        UUID id = UUID.randomUUID();
        AdminUserDetail expected = buildDetail(RoleType.INFLUENCER);
        when(userPersistence.updateVerified(id, true)).thenReturn(expected);

        userService.updateVerified(id, true);

        verify(userPersistence).updateVerified(id, true);
    }

    @Test
    void updateVerified_returnsUpdatedDetail() {
        UUID id = UUID.randomUUID();
        AdminUserDetail expected = AdminUserDetail.builder()
                .id(id).name("Test").email("t@t.com").verified(true).role(RoleType.INFLUENCER).build();
        when(userPersistence.updateVerified(id, true)).thenReturn(expected);

        AdminUserDetail result = userService.updateVerified(id, true);

        assertNotNull(result);
        assertTrue(result.getVerified());
    }

    @Test
    void updateVerified_shouldNotCallFindAll() {
        UUID id = UUID.randomUUID();
        when(userPersistence.updateVerified(id, true)).thenReturn(buildDetail(RoleType.BUSINESS));

        userService.updateVerified(id, true);

        verify(userPersistence, never()).findAll(any(), any());
    }

    // -------------------------------------------------------------------------
    //  deleteUser
    // -------------------------------------------------------------------------

    @Test
    void deleteUser_shouldDelegateToPersistence() {
        UUID id = UUID.randomUUID();
        String adminEmail = "admin@linkal.es";

        userService.deleteUser(id, adminEmail);

        verify(userPersistence).deleteUser(id, adminEmail);
    }

    @Test
    void deleteUser_shouldNotCallFindAll() {
        UUID id = UUID.randomUUID();

        userService.deleteUser(id, "admin@linkal.es");

        verify(userPersistence, never()).findAll(any(), any());
    }

    @Test
    void deleteUser_shouldNotCallFindById() {
        UUID id = UUID.randomUUID();

        userService.deleteUser(id, "admin@linkal.es");

        verify(userPersistence, never()).findById(any());
    }

    // --------------------------------------------------------------------------
    //  helpers
    // --------------------------------------------------------------------------

    private AdminUserDetail buildDetail(RoleType role) {
        return AdminUserDetail.builder()
                .id(UUID.randomUUID())
                .name("Usuario Test")
                .email("test@linkal.es")
                .verified(true)
                .role(role)
                .build();
    }
}