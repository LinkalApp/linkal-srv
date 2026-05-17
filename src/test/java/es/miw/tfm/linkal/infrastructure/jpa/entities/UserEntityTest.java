package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.User;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserEntityTest {
    // -------------------------------------------------------------------------
    //  Constructor UserEntity(User)
    // -------------------------------------------------------------------------

    @Test
    void constructor_fromUser_shouldCopyAllBaseFields() {
        User user = buildUser();

        UserEntity entity = new UserEntity(user);

        assertEquals("Irene Test", entity.getName());
        assertEquals("irene@test.com", entity.getEmail());
        assertEquals("hashedPass", entity.getPassword());
        assertEquals("600123456", entity.getPhoneNumber());
        assertEquals("Una descripción", entity.getDescription());
    }

    @Test
    void constructor_fromUser_shouldCopyVerifiedField() {
        User user = buildUser();
        user.setVerified(true);

        UserEntity entity = new UserEntity(user);

        assertTrue(entity.getVerified());
    }

    @Test
    void constructor_fromUser_shouldCopyRoleField() {
        User user = buildUser();
        user.setRole(RoleType.BUSINESS);

        UserEntity entity = new UserEntity(user);

        assertEquals(RoleType.BUSINESS, entity.getRole());
    }

    @Test
    void constructor_fromUser_shouldHandleNullOptionalFields() {
        User user = new User();
        user.setName("Min User");
        user.setEmail("min@test.com");
        user.setPassword("pass");

        UserEntity entity = new UserEntity(user);

        assertEquals("Min User", entity.getName());
        assertNull(entity.getPhoneNumber());
        assertNull(entity.getDescription());
    }

    // -------------------------------------------------------------------------
    //  toUser()
    // -------------------------------------------------------------------------

    @Test
    void toUser_shouldMapAllFields() {
        UserEntity entity = new UserEntity();
        UUID id = UUID.randomUUID();
        entity.setId(id);
        entity.setName("Irene Test");
        entity.setEmail("irene@test.com");
        entity.setPassword("hashedPass");
        entity.setPhoneNumber("600123456");
        entity.setDescription("Una descripción");
        entity.setVerified(false);

        User user = entity.toUser();

        assertEquals(id, user.getId());
        assertEquals("Irene Test", user.getName());
        assertEquals("irene@test.com", user.getEmail());
        assertEquals("hashedPass", user.getPassword());
        assertEquals("600123456", user.getPhoneNumber());
        assertEquals("Una descripción", user.getDescription());
        assertFalse(user.getVerified());
    }

    @Test
    void toUser_shouldReturnNewInstanceEachTime() {
        UserEntity entity = new UserEntity();
        entity.setName("Test");
        entity.setEmail("test@test.com");
        entity.setPassword("pass");

        User user1 = entity.toUser();
        User user2 = entity.toUser();

        assertNotSame(user1, user2);
    }

    // -------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    private User buildUser() {
        User user = new User();
        user.setName("Irene Test");
        user.setEmail("irene@test.com");
        user.setPassword("hashedPass");
        user.setPhoneNumber("600123456");
        user.setDescription("Una descripción");
        user.setVerified(false);
        return user;
    }
}
