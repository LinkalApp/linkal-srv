package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.infrastructure.jpa.repositories.EvaluationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationPersistenceJpaTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @InjectMocks
    private EvaluationPersistenceJpa evaluationPersistenceJpa;

    // -------------------------------------------------------------------------
    //  averageScoreByInfluencerId
    // -------------------------------------------------------------------------

    @Test
    void averageScoreByInfluencerId_shouldReturnAverageFromRepository() {
        UUID influencerId = UUID.randomUUID();
        when(evaluationRepository.findAverageScoreByValuedUserId(influencerId)).thenReturn(3.75);

        Double result = evaluationPersistenceJpa.averageScoreByInfluencerId(influencerId);

        assertEquals(3.75, result);
        verify(evaluationRepository).findAverageScoreByValuedUserId(influencerId);
    }

    @Test
    void averageScoreByInfluencerId_shouldReturnNullWhenNoEvaluations() {
        UUID influencerId = UUID.randomUUID();
        when(evaluationRepository.findAverageScoreByValuedUserId(influencerId)).thenReturn(null);

        Double result = evaluationPersistenceJpa.averageScoreByInfluencerId(influencerId);

        assertNull(result);
    }

    @Test
    void averageScoreByInfluencerId_shouldDelegateToRepositoryWithCorrectId() {
        UUID influencerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();

        when(evaluationRepository.findAverageScoreByValuedUserId(influencerId)).thenReturn(5.0);
        when(evaluationRepository.findAverageScoreByValuedUserId(otherId)).thenReturn(1.0);

        Double r1 = evaluationPersistenceJpa.averageScoreByInfluencerId(influencerId);
        Double r2 = evaluationPersistenceJpa.averageScoreByInfluencerId(otherId);

        assertEquals(5.0, r1);
        assertEquals(1.0, r2);
    }

    @Test
    void averageScoreByInfluencerId_shouldReturnPerfectScore() {
        UUID influencerId = UUID.randomUUID();
        when(evaluationRepository.findAverageScoreByValuedUserId(influencerId)).thenReturn(5.0);

        Double result = evaluationPersistenceJpa.averageScoreByInfluencerId(influencerId);

        assertEquals(5.0, result);
    }

    @Test
    void averageScoreByInfluencerId_shouldReturnMinimumScore() {
        UUID influencerId = UUID.randomUUID();
        when(evaluationRepository.findAverageScoreByValuedUserId(influencerId)).thenReturn(1.0);

        Double result = evaluationPersistenceJpa.averageScoreByInfluencerId(influencerId);

        assertEquals(1.0, result);
    }
}
