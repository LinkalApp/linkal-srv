package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.domain.model.Chat;
import es.miw.tfm.linkal.domain.model.Message;
import es.miw.tfm.linkal.domain.services.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Rest
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatResource {

    private final ChatService chatService;

    @GetMapping
    @PreAuthorize("hasAnyRole('INFLUENCER','BUSINESS')")
    public ResponseEntity<List<Chat>> findAllByUser(Authentication authentication) {
        return ResponseEntity.ok(chatService.findAllByUser(authentication.getName()));
    }

    @PostMapping("/{chatId}/messages")
    @PreAuthorize("hasAnyRole('INFLUENCER','BUSINESS')")
    public ResponseEntity<Message> sendMessage(@PathVariable UUID chatId,
                                               @Valid @RequestBody Message request,
                                               Authentication authentication) {
        return ResponseEntity.status(201)
                .body(chatService.sendMessage(chatId, request.getText(), authentication.getName()));
    }

    @GetMapping("/{chatId}/messages")
    @PreAuthorize("hasAnyRole('INFLUENCER','BUSINESS')")
    public ResponseEntity<List<Message>> getMessages(@PathVariable UUID chatId,
                                                     Authentication authentication) {
        return ResponseEntity.ok(chatService.getMessages(chatId, authentication.getName()));
    }
}
