package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.domain.model.Business;
import es.miw.tfm.linkal.domain.services.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Rest
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessResource {
    private final BusinessService businessService;

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
}
