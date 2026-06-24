package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.services.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<Campaign> update(@PathVariable UUID id,
                                           @RequestBody Campaign campaign,
                                           Authentication authentication) {
        return ResponseEntity.ok(campaignService.update(id, campaign, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       Authentication authentication) {
        campaignService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/open")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<java.util.List<Campaign>> findAllOpen( @RequestParam(required = false) String category,
                                                                 @RequestParam(required = false) String province) {
        boolean hasFilter = (category != null && !category.isBlank())
                || (province != null && !province.isBlank());
        List<Campaign> result = new ArrayList<>();
        if (hasFilter) {
            result = campaignService.findOpenByFilters(category, province);
        } else {
            result = campaignService.findAllOpen();
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{campaignId}/start/{matchId}")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<Campaign> startWithInfluencer(@PathVariable UUID campaignId,
                                                        @PathVariable UUID matchId,
                                                        Authentication authentication) {
        return ResponseEntity.ok(campaignService.startWithInfluencer(campaignId, matchId, authentication.getName()));
    }
}
