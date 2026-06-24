package es.miw.tfm.linkal.infrastructure.resources;

import es.miw.tfm.linkal.domain.model.Evaluation;
import es.miw.tfm.linkal.domain.services.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Rest
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationResource {

    private final EvaluationService evaluationService;

    @PostMapping("/matches/{matchId}")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<Evaluation> create(@PathVariable UUID matchId,
                                             @RequestBody Evaluation evaluation,
                                             Authentication authentication) {
        return ResponseEntity.status(201)
                .body(evaluationService.create(evaluation, matchId, authentication.getName()));
    }

    @PostMapping("/matches/{matchId}/influencer")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Evaluation> createByInfluencer(@PathVariable UUID matchId,
                                                         @RequestBody Evaluation evaluation,
                                                         Authentication authentication) {
        return ResponseEntity.status(201)
                .body(evaluationService.createByInfluencer(evaluation, matchId, authentication.getName()));
    }
}