package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.Evaluation;
import es.miw.tfm.linkal.domain.model.enums.MatchStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluationEntityTest {

    // -------------------------------------------------------------------------
    //  toEvaluation()
    // -------------------------------------------------------------------------

    @Test
    void toEvaluation_shouldMapIdAndScore() {
        UUID id = UUID.randomUUID();

        EvaluationEntity entity = EvaluationEntity.builder()
                .id(id)
                .score(5)
                .idUserValued(UUID.randomUUID())
                .build();

        Evaluation evaluation = entity.toEvaluation();

        assertEquals(id, evaluation.getId());
        assertEquals(5, evaluation.getScore());
    }

    @Test
    void toEvaluation_shouldSetMatchId_whenMatchIsNotNull() {
        UUID matchId = UUID.randomUUID();
        MatchEntity match = MatchEntity.builder()
                .id(matchId)
                .createdAt(LocalDateTime.now())
                .status(MatchStatus.COMPLETED)
                .build();

        EvaluationEntity entity = EvaluationEntity.builder()
                .id(UUID.randomUUID())
                .score(4)
                .idUserValued(UUID.randomUUID())
                .match(match)
                .build();

        Evaluation evaluation = entity.toEvaluation();

        assertEquals(matchId, evaluation.getMatchId());
    }

    @Test
    void toEvaluation_shouldLeaveMatchIdNull_whenMatchIsNull() {
        EvaluationEntity entity = EvaluationEntity.builder()
                .id(UUID.randomUUID())
                .score(3)
                .idUserValued(UUID.randomUUID())
                .build();

        Evaluation evaluation = entity.toEvaluation();

        assertNull(evaluation.getMatchId());
    }

    @Test
    void toEvaluation_shouldMapDifferentScoreValues() {
        EvaluationEntity entity1 = EvaluationEntity.builder()
                .id(UUID.randomUUID()).score(1).idUserValued(UUID.randomUUID()).build();
        EvaluationEntity entity5 = EvaluationEntity.builder()
                .id(UUID.randomUUID()).score(5).idUserValued(UUID.randomUUID()).build();

        assertEquals(1, entity1.toEvaluation().getScore());
        assertEquals(5, entity5.toEvaluation().getScore());
    }

    @Test
    void toEvaluation_shouldReturnNewInstanceEachTime() {
        EvaluationEntity entity = EvaluationEntity.builder()
                .id(UUID.randomUUID())
                .score(4)
                .idUserValued(UUID.randomUUID())
                .build();

        Evaluation e1 = entity.toEvaluation();
        Evaluation e2 = entity.toEvaluation();

        assertNotSame(e1, e2);
    }
}
