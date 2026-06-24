package es.miw.tfm.linkal.domain.services;

import es.miw.tfm.linkal.domain.model.Evaluation;
import es.miw.tfm.linkal.domain.persistence.EvaluationPersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluationPersistence evaluationPersistence;

    @InjectMocks
    private EvaluationService evaluationService;

    // -----------------------------------------------------------------------------
    //  create
    // -----------------------------------------------------------------------------

    @Test
    void create_shouldDelegateToPersistence() {
        Evaluation evaluation = buildEvaluation();
        UUID matchId = UUID.randomUUID();
        when(evaluationPersistence.create(evaluation, matchId, "business@test.com")).thenReturn(buildSavedEvaluation());

        evaluationService.create(evaluation, matchId, "business@test.com");

        verify(evaluationPersistence).create(evaluation, matchId, "business@test.com");
    }

    @Test
    void create_shouldReturnEvaluationFromPersistence() {
        Evaluation evaluation = buildEvaluation();
        UUID matchId = UUID.randomUUID();
        Evaluation saved = buildSavedEvaluation();

        when(evaluationPersistence.create(any(), eq(matchId), any())).thenReturn(saved);

        Evaluation result = evaluationService.create(evaluation, matchId, "business@test.com");

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(saved.getId(), result.getId());
        assertEquals(5, result.getScore());
    }

    @Test
    void create_shouldPassEmailToPersistence() {
        UUID matchId = UUID.randomUUID();
        when(evaluationPersistence.create(any(), any(), any())).thenReturn(buildSavedEvaluation());

        evaluationService.create(buildEvaluation(), matchId, "otro@empresa.com");

        verify(evaluationPersistence).create(any(), eq(matchId), eq("otro@empresa.com"));
    }

    @Test
    void create_shouldPassMatchIdToPersistence() {
        UUID matchId = UUID.randomUUID();
        when(evaluationPersistence.create(any(), any(), any())).thenReturn(buildSavedEvaluation());

        evaluationService.create(buildEvaluation(), matchId, "business@test.com");

        verify(evaluationPersistence).create(any(), eq(matchId), any());
    }

    @Test
    void create_shouldNotCallAverageScore() {
        UUID matchId = UUID.randomUUID();
        when(evaluationPersistence.create(any(), any(), any())).thenReturn(buildSavedEvaluation());

        evaluationService.create(buildEvaluation(), matchId, "business@test.com");

        verify(evaluationPersistence, never()).averageScoreByInfluencerId(any());
        verify(evaluationPersistence, never()).averageScoreByBusinessId(any());
    }

    @Test
    void create_withMinScore_shouldDelegate() {
        Evaluation evaluation = Evaluation.builder().score(1).build();
        UUID matchId = UUID.randomUUID();
        Evaluation saved = buildSavedEvaluation();
        saved.setScore(1);
        when(evaluationPersistence.create(any(), any(), any())).thenReturn(saved);

        Evaluation result = evaluationService.create(evaluation, matchId, "business@test.com");

        assertEquals(1, result.getScore());
    }

    // --------------------------------------------------------------------------
    //  createByInfluencer
    // ---------------------------------------------------------------------------

    @Test
    void createByInfluencer_shouldDelegateToPersistence() {
        Evaluation evaluation = buildEvaluation();
        UUID matchId = UUID.randomUUID();
        when(evaluationPersistence.createByInfluencer(evaluation, matchId, "influencer@test.com")).thenReturn(buildSavedEvaluation());

        evaluationService.createByInfluencer(evaluation, matchId, "influencer@test.com");

        verify(evaluationPersistence).createByInfluencer(evaluation, matchId, "influencer@test.com");
    }

    @Test
    void createByInfluencer_shouldReturnEvaluationFromPersistence() {
        Evaluation evaluation = buildEvaluation();
        UUID matchId = UUID.randomUUID();
        Evaluation saved = buildSavedEvaluation();

        when(evaluationPersistence.createByInfluencer(any(), eq(matchId), any())).thenReturn(saved);

        Evaluation result = evaluationService.createByInfluencer(evaluation, matchId, "influencer@test.com");

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals(5, result.getScore());
    }

    @Test
    void createByInfluencer_shouldPassEmailToPersistence() {
        UUID matchId = UUID.randomUUID();
        when(evaluationPersistence.createByInfluencer(any(), any(), any())).thenReturn(buildSavedEvaluation());

        evaluationService.createByInfluencer(buildEvaluation(), matchId, "otro@influencer.com");

        verify(evaluationPersistence).createByInfluencer(any(), eq(matchId), eq("otro@influencer.com"));
    }

    @Test
    void createByInfluencer_shouldPassMatchIdToPersistence() {
        UUID matchId = UUID.randomUUID();
        when(evaluationPersistence.createByInfluencer(any(), any(), any())).thenReturn(buildSavedEvaluation());

        evaluationService.createByInfluencer(buildEvaluation(), matchId, "influencer@test.com");

        verify(evaluationPersistence).createByInfluencer(any(), eq(matchId), any());
    }

    @Test
    void createByInfluencer_shouldNotCallCreate() {
        UUID matchId = UUID.randomUUID();
        when(evaluationPersistence.createByInfluencer(any(), any(), any())).thenReturn(buildSavedEvaluation());

        evaluationService.createByInfluencer(buildEvaluation(), matchId, "influencer@test.com");

        verify(evaluationPersistence, never()).create(any(), any(), any());
    }


    // ----------------------------------------------------------------------------
    //  helpers
    // ---------------------------------------------------------------------------

    private Evaluation buildEvaluation() {
        return Evaluation.builder().score(5).build();
    }

    private Evaluation buildSavedEvaluation() {
        return Evaluation.builder()
                .id(UUID.randomUUID())
                .score(5)
                .valuedUserId(UUID.randomUUID())
                .matchId(UUID.randomUUID())
                .build();
    }
}