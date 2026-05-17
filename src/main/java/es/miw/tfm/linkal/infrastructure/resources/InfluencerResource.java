package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.domain.model.Influencer;
import es.miw.tfm.linkal.domain.services.InfluencerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
