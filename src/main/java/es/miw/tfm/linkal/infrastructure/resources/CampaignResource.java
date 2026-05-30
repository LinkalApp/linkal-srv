package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.services.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@Rest
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignResource {
    private final CampaignService campaignService;

    @PostMapping
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<Campaign> create(@Valid @RequestBody Campaign campaign,
                                           Authentication authentication) {
        return ResponseEntity.status(201)
                .body(campaignService.create(campaign, authentication.getName()));
    }
}
