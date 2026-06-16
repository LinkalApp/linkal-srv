package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.configuration.JwtService;
import es.miw.tfm.linkal.configuration.SecurityConfiguration;
import es.miw.tfm.linkal.domain.exceptions.BadRequestException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Chat;
import es.miw.tfm.linkal.domain.services.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatResource.class)
@Import(SecurityConfiguration.class)
public class ChatResourceTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean private ChatService chatService;
    @MockitoBean
    private JwtService jwtService;

    // GET /api/chats ------------------------------------------------------------

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void findAllByUser_shouldReturn200WithEmptyList() throws Exception {
        when(chatService.findAllByUser("influencer@test.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void findAllByUser_shouldReturn200WithChats() throws Exception {
        UUID matchId = UUID.randomUUID();
        Chat chat = buildChatWithDisplayName(matchId, "Nike Spain", "Hola!");

        when(chatService.findAllByUser(eq("influencer@test.com"))).thenReturn(List.of(chat));

        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].displayName").value("Nike Spain"))
                .andExpect(jsonPath("$[0].lastMessage").value("Hola!"));
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    void findAllByUser_shouldReturn200ForBusinessRole() throws Exception {
        UUID matchId = UUID.randomUUID();
        when(chatService.findAllByUser("business@test.com"))
                .thenReturn(List.of(buildChatWithDisplayName(matchId, "Ana López", null)));

        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].displayName").value("Ana López"));
    }

    @Test
    void findAllByUser_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void findAllByUser_shouldReturn200WithCampaignTitle() throws Exception {
        UUID matchId = UUID.randomUUID();
        Chat chat = Chat.builder()
                .id(UUID.randomUUID())
                .matchId(matchId)
                .displayName("Nike Spain")
                .campaignTitle("Campaña Verano 2025")
                .lastMessage("Hola!")
                .build();

        when(chatService.findAllByUser("influencer@test.com")).thenReturn(List.of(chat));

        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value("Nike Spain"))
                .andExpect(jsonPath("$[0].campaignTitle").value("Campaña Verano 2025"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void findAllByUser_shouldReturn200WithTwoChatsFromSameBusiness() throws Exception {
        UUID m1 = UUID.randomUUID(), m2 = UUID.randomUUID();
        Chat chat1 = Chat.builder().id(UUID.randomUUID()).matchId(m1)
                .displayName("Nike Spain").campaignTitle("Campaña Verano").build();
        Chat chat2 = Chat.builder().id(UUID.randomUUID()).matchId(m2)
                .displayName("Nike Spain").campaignTitle("Colección Otoño").build();

        when(chatService.findAllByUser("influencer@test.com")).thenReturn(List.of(chat1, chat2));

        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].campaignTitle").value("Campaña Verano"))
                .andExpect(jsonPath("$[1].campaignTitle").value("Colección Otoño"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com", roles = "INFLUENCER")
    void findAllByUser_shouldReturn200WithMultipleChats() throws Exception {
        UUID m1 = UUID.randomUUID(), m2 = UUID.randomUUID();
        when(chatService.findAllByUser("influencer@test.com"))
                .thenReturn(List.of(
                        buildChatWithDisplayName(m1, "Nike", "Hola"),
                        buildChatWithDisplayName(m2, "Adidas", "Ok")
                ));

        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // helpers -------------------------------------------------------------------------------

    private Chat buildChatWithDisplayName(UUID matchId, String displayName, String lastMessage) {
        return Chat.builder()
                .id(UUID.randomUUID())
                .name("Chat test")
                .matchId(matchId)
                .displayName(displayName)
                .lastMessage(lastMessage)
                .lastMessageAt(lastMessage != null ? LocalDateTime.now() : null)
                .build();
    }
}
