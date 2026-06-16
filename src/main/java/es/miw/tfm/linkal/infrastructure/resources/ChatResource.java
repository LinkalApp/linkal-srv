package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.domain.model.Chat;
import es.miw.tfm.linkal.domain.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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
}
