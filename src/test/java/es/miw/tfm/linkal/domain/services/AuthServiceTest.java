package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.configuration.JwtService;
import es.miw.tfm.linkal.domain.exceptions.BadRequestException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import es.miw.tfm.linkal.infrastructure.jpa.entities.PasswordResetTokenEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.UserEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.PasswordResetTokenRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

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

    // ------------------------------------------------------------------------
    //  sendResetCode
    // --------------------------------------------------------------------------

    @Test
    void sendResetCode_shouldThrowNotFoundWhenEmailDoesNotExist() {
        when(userRepository.existsByEmail("unknown@test.com")).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> authService.sendResetCode("unknown@test.com"));

        verifyNoInteractions(tokenRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void sendResetCode_shouldDeleteExistingTokenBeforeCreatingNew() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.sendResetCode("user@test.com");

        verify(tokenRepository).deleteByEmail("user@test.com");
    }

    @Test
    void sendResetCode_shouldSaveTokenWithCorrectEmail() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.sendResetCode("user@test.com");

        ArgumentCaptor<PasswordResetTokenEntity> captor =
                ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(tokenRepository).save(captor.capture());
        assertEquals("user@test.com", captor.getValue().getEmail());
    }

    @Test
    void sendResetCode_shouldSaveTokenWithSixDigitCode() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.sendResetCode("user@test.com");

        ArgumentCaptor<PasswordResetTokenEntity> captor =
                ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(tokenRepository).save(captor.capture());
        String code = captor.getValue().getCode();
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    void sendResetCode_shouldSaveTokenWithFutureExpiryDate() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.sendResetCode("user@test.com");

        ArgumentCaptor<PasswordResetTokenEntity> captor =
                ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(tokenRepository).save(captor.capture());
        assertTrue(captor.getValue().getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void sendResetCode_shouldCallEmailServiceWithCorrectParams() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.sendResetCode("user@test.com");

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> codeCaptor  = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetCode(emailCaptor.capture(), codeCaptor.capture());
        assertEquals("user@test.com", emailCaptor.getValue());
        assertTrue(codeCaptor.getValue().matches("\\d{6}"));
    }

    @Test
    void sendResetCode_shouldNotThrowWhenEmailServiceFails() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("SMTP error")).when(emailService)
                .sendPasswordResetCode(any(), any());

        // El try-catch interno absorbe el error de email; el token ya se habrá guardado
        assertDoesNotThrow(() -> authService.sendResetCode("user@test.com"));
        verify(tokenRepository).save(any());
    }

    // -------------------------------------------------------------------------
    //  resetPassword
    // -------------------------------------------------------------------------

    @Test
    void resetPassword_shouldThrowBadRequestWhenNoActiveTokenExists() {
        when(tokenRepository.findByEmailAndUsedFalse("user@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> authService.resetPassword("user@test.com", "123456", "newPass"));
    }

    @Test
    void resetPassword_shouldThrowBadRequestWhenTokenIsExpired() {
        PasswordResetTokenEntity token = buildToken("user@test.com", "123456",
                LocalDateTime.now().minusMinutes(1), false);
        when(tokenRepository.findByEmailAndUsedFalse("user@test.com"))
                .thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class,
                () -> authService.resetPassword("user@test.com", "123456", "newPass"));
    }

    @Test
    void resetPassword_shouldThrowBadRequestWhenCodeIsWrong() {
        PasswordResetTokenEntity token = buildToken("user@test.com", "123456",
                LocalDateTime.now().plusMinutes(10), false);
        when(tokenRepository.findByEmailAndUsedFalse("user@test.com"))
                .thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class,
                () -> authService.resetPassword("user@test.com", "000000", "newPass"));
    }

    @Test
    void resetPassword_shouldEncodeAndSaveNewPassword() {
        PasswordResetTokenEntity token = buildToken("user@test.com", "123456",
                LocalDateTime.now().plusMinutes(10), false);
        UserEntity user = buildUserEntity("user@test.com", "oldHashed", RoleType.BUSINESS);

        when(tokenRepository.findByEmailAndUsedFalse("user@test.com"))
                .thenReturn(Optional.of(token));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("newHashed");

        authService.resetPassword("user@test.com", "123456", "newPass");

        assertEquals("newHashed", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_shouldMarkTokenAsUsed() {
        PasswordResetTokenEntity token = buildToken("user@test.com", "123456",
                LocalDateTime.now().plusMinutes(10), false);
        UserEntity user = buildUserEntity("user@test.com", "oldHashed", RoleType.BUSINESS);

        when(tokenRepository.findByEmailAndUsedFalse("user@test.com"))
                .thenReturn(Optional.of(token));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        authService.resetPassword("user@test.com", "123456", "newPass");

        assertTrue(token.isUsed());
        verify(tokenRepository).save(token);
    }

    @Test
    void resetPassword_shouldThrowNotFoundWhenUserDisappearsAfterToken() {
        PasswordResetTokenEntity token = buildToken("ghost@test.com", "123456",
                LocalDateTime.now().plusMinutes(10), false);
        when(tokenRepository.findByEmailAndUsedFalse("ghost@test.com"))
                .thenReturn(Optional.of(token));
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> authService.resetPassword("ghost@test.com", "123456", "newPass"));
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

    private PasswordResetTokenEntity buildToken(String email, String code,
                                                LocalDateTime expiresAt, boolean used) {
        return PasswordResetTokenEntity.builder()
                .email(email)
                .code(code)
                .expiresAt(expiresAt)
                .used(used)
                .build();
    }
}
