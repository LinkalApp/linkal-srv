package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.configuration.JwtService;
import es.miw.tfm.linkal.domain.exceptions.BadRequestException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.infrastructure.jpa.entities.PasswordResetTokenEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.UserEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.PasswordResetTokenRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int CODE_EXPIRY_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public record AuthResponse(String token, String role, String email) {}

    // Login ----------------------------------------------------------------------------------
    public AuthResponse login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getEmail());
    }

    // Forgot password ---------------------------------------------------------------------

    @Transactional
    public void sendResetCode(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new NotFoundException("User not found: " + email);
        }

        tokenRepository.deleteByEmail(email);

        String code = String.format("%06d", new Random().nextInt(1_000_000));

        PasswordResetTokenEntity token = PasswordResetTokenEntity.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES))
                .build();
        tokenRepository.save(token);

        try {
            emailService.sendPasswordResetCode(email, code);
        } catch (Exception e) {
            log.warn("No se pudo enviar el email de recuperación a {}. Código generado: {} — Error: {}",
                    email, code, e.getMessage());
        }
    }

    // Reset password ----------------------------------------------------------------------------

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        PasswordResetTokenEntity token = tokenRepository
                .findByEmailAndUsedFalse(email)
                .orElseThrow(() -> new BadRequestException("No existe un código de recuperación activo para este email"));

        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new BadRequestException("El código ha expirado");
        }
        if (!token.getCode().equals(code)) {
            throw new BadRequestException("Código incorrecto");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
    }
}
