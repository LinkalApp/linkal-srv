package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.domain.model.Match;
import es.miw.tfm.linkal.domain.services.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Rest
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchResource {
    private final MatchService matchService;

    @PostMapping("/campaigns/{campaignId}")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Match> createByInfluencer(@PathVariable UUID campaignId, Authentication authentication) {
        return ResponseEntity.status(201)
                .body(matchService.createByInfluencer(campaignId, authentication.getName()));
    }

    @GetMapping("/campaigns/{campaignId}/influencer")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Match> findByInfluencer(@PathVariable UUID campaignId,
                                                  Authentication authentication) {
        return matchService.findByInfluencer(campaignId, authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
