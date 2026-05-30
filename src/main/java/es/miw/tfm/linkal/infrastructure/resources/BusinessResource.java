package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.domain.model.Business;
import es.miw.tfm.linkal.domain.model.Campaign;
import es.miw.tfm.linkal.domain.model.Influencer;
import es.miw.tfm.linkal.domain.services.BusinessService;
import es.miw.tfm.linkal.domain.services.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Rest
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessResource {
    private final BusinessService businessService;
    private final CampaignService campaignService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody Business business) {
        businessService.create(business);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<Business> readMe(Authentication authentication) {
        return ResponseEntity.ok(businessService.readMe(authentication.getName()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<Business> updateMe(Authentication authentication,
                                               @RequestBody Business business) {
        return ResponseEntity.ok(businessService.updateMe(authentication.getName(), business));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<Void> deleteMe(Authentication authentication) {
        businessService.deleteMe(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/campaigns")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Campaign>> getCampaigns(@PathVariable UUID id) {
        return ResponseEntity.ok(campaignService.findByBusinessId(id));
    }
}
