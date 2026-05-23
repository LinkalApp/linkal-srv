package es.miw.tfm.linkal.domain.services;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${linkal.mail.from}")
    private String fromEmail;

    public void sendPasswordResetCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Linkal – Código de recuperación de contraseña");
        message.setText(
                "Hola,\n\n" +
                        "Has solicitado recuperar tu contraseña en Linkal.\n\n" +
                        "Tu código de verificación es:\n\n" +
                        "    " + code + "\n\n" +
                        "Este código es válido durante 15 minutos.\n" +
                        "Si no has solicitado este código, ignora este mensaje.\n\n" +
                        "El equipo de Linkal"
        );
        mailSender.send(message);
    }
}
