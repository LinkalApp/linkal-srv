package es.miw.tfm.linkal.domain.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@linkal.es");
    }

    // -------------------------------------------------------------------------
    //  sendPasswordResetCode
    // -------------------------------------------------------------------------

    @Test
    void sendPasswordResetCode_shouldCallMailSenderSend() {
        emailService.sendPasswordResetCode("user@test.com", "123456");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPasswordResetCode_shouldSendToCorrectRecipient() {
        emailService.sendPasswordResetCode("user@test.com", "654321");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertNotNull(sent.getTo());
        assertEquals(1, sent.getTo().length);
        assertEquals("user@test.com", sent.getTo()[0]);
    }

    @Test
    void sendPasswordResetCode_shouldSetFromAddress() {
        emailService.sendPasswordResetCode("user@test.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertEquals("noreply@linkal.es", captor.getValue().getFrom());
    }

    @Test
    void sendPasswordResetCode_shouldIncludeCodeInBody() {
        emailService.sendPasswordResetCode("user@test.com", "987654");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String body = captor.getValue().getText();
        assertNotNull(body);
        assertTrue(body.contains("987654"));
    }

    @Test
    void sendPasswordResetCode_shouldHaveNonBlankSubject() {
        emailService.sendPasswordResetCode("user@test.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String subject = captor.getValue().getSubject();
        assertNotNull(subject);
        assertFalse(subject.isBlank());
    }

    @Test
    void sendPasswordResetCode_shouldPropagateExceptionFromMailSender() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class,
                () -> emailService.sendPasswordResetCode("user@test.com", "123456"));
    }
}

