package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.Evaluation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "evaluations")
public class EvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private UUID idUserValued;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    public Evaluation toEvaluation() {
        Evaluation evaluation = new Evaluation();
        BeanUtils.copyProperties(this, evaluation);
        if (this.match != null) {
            evaluation.setMatchId(this.match.getId());
        }
        return evaluation;
    }
}