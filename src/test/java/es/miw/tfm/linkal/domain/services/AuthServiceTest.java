package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.configuration.JwtService;
import es.miw.tfm.linkal.domain.exceptions.BadRequestException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import es.miw.tfm.linkal.infrastructure.jpa.entities.UserEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    // -------------------------------------------------------------------------
    //  login — casos de éxito
    // -------------------------------------------------------------------------

    @Test
    void login_shouldReturnAuthResponseForBusiness() {
        UserEntity user = buildUserEntity("business@test.com", "hashed", RoleType.BUSINESS);

        when(userRepository.findByEmail("business@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPass", "hashed")).thenReturn(true);
        when(jwtService.generateToken("business@test.com", "BUSINESS")).thenReturn("jwt-token");

        AuthService.AuthResponse response = authService.login("business@test.com", "rawPass");

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("BUSINESS", response.role());
        assertEquals("business@test.com", response.email());
    }

    @Test
    void login_shouldReturnAuthResponseForInfluencer() {
        UserEntity user = buildUserEntity("influencer@test.com", "hashed", RoleType.INFLUENCER);

        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPass", "hashed")).thenReturn(true);
        when(jwtService.generateToken("influencer@test.com", "INFLUENCER")).thenReturn("jwt-influencer");

        AuthService.AuthResponse response = authService.login("influencer@test.com", "rawPass");

        assertEquals("INFLUENCER", response.role());
        assertEquals("jwt-influencer", response.token());
    }

    @Test
    void login_shouldCallJwtServiceWithCorrectArguments() {
        UserEntity user = buildUserEntity("user@test.com", "hashed", RoleType.BUSINESS);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtService.generateToken(any(), any())).thenReturn("token");

        authService.login("user@test.com", "pass");

        verify(jwtService).generateToken("user@test.com", "BUSINESS");
    }

    // -----------------------------------------------------------------------
    //  login — casos de error
    // ------------------------------------------------------------------------

    @Test
    void login_shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> authService.login("unknown@test.com", "anyPass"));

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void login_shouldThrowBadRequestExceptionWhenPasswordIsWrong() {
        UserEntity user = buildUserEntity("user@test.com", "hashed", RoleType.BUSINESS);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "hashed")).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> authService.login("user@test.com", "wrongPass"));

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void login_shouldNotGenerateTokenOnFailedAuthentication() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> authService.login("x@test.com", "pass"));

        verifyNoInteractions(jwtService);
    }

    // -------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    private UserEntity buildUserEntity(String email, String hashedPassword, RoleType role) {
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Test User");
        entity.setEmail(email);
        entity.setPassword(hashedPassword);
        entity.setVerified(false);
        try {
            var roleField = UserEntity.class.getDeclaredField("role");
            roleField.setAccessible(true);
            roleField.set(entity, role);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return entity;
    }
}
