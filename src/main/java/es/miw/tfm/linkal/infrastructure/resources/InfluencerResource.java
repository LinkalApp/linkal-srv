package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.domain.model.Influencer;
import es.miw.tfm.linkal.domain.services.InfluencerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Rest
@RequestMapping("/api/influencers")
@RequiredArgsConstructor
public class InfluencerResource {
    private final InfluencerService influencerService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody Influencer influencer) {
        influencerService.create(influencer);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Influencer> readMe(Authentication authentication) {
        return ResponseEntity.ok(influencerService.readMe(authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Influencer>> readAll( @RequestParam(required = false) List<String> interests) {
        List<Influencer> result = new ArrayList<>();
        if(interests != null && !interests.isEmpty()){
            result = influencerService.findByInterests(interests);
        } else {
            result = influencerService.readAll();
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Influencer> updateMe(Authentication authentication,
                                               @RequestBody Influencer influencer) {
        return ResponseEntity.ok(influencerService.updateMe(authentication.getName(), influencer));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Void> deleteMe(Authentication authentication) {
        influencerService.deleteMe(authentication.getName());
        return ResponseEntity.noContent().build();
    }


}
