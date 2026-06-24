package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.ConflictException;
import es.miw.tfm.linkal.domain.exceptions.ForbiddenException;
import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.Evaluation;
import es.miw.tfm.linkal.domain.model.enums.CampaignStatus;
import es.miw.tfm.linkal.infrastructure.jpa.entities.*;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.BusinessRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.EvaluationRepository;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.ExpectedCount.never;

@ExtendWith(MockitoExtension.class)
public class EvaluationPersistenceJpaTest {

    @Mock
    private EvaluationRepository evaluationRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private BusinessRepository businessRepository;

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

    // ------------------------------------------------------------------------
    //  averageScoreByBusinessId
    // -------------------------------------------------------------------------

    @Test
    void averageScoreByBusinessId_shouldReturnAverageFromRepository() {
        UUID businessId = UUID.randomUUID();
        when(evaluationRepository.findAverageScoreByValuedUserId(businessId)).thenReturn(4.5);

        Double result = evaluationPersistenceJpa.averageScoreByBusinessId(businessId);

        assertEquals(4.5, result);
        verify(evaluationRepository).findAverageScoreByValuedUserId(businessId);
    }

    @Test
    void averageScoreByBusinessId_shouldReturnNullWhenNoEvaluations() {
        UUID businessId = UUID.randomUUID();
        when(evaluationRepository.findAverageScoreByValuedUserId(businessId)).thenReturn(null);

        Double result = evaluationPersistenceJpa.averageScoreByBusinessId(businessId);

        assertNull(result);
    }

    @Test
    void averageScoreByBusinessId_shouldDelegateToRepositoryWithCorrectId() {
        UUID businessId = UUID.randomUUID();
        UUID otherId    = UUID.randomUUID();

        when(evaluationRepository.findAverageScoreByValuedUserId(businessId)).thenReturn(3.0);
        when(evaluationRepository.findAverageScoreByValuedUserId(otherId)).thenReturn(2.0);

        Double r1 = evaluationPersistenceJpa.averageScoreByBusinessId(businessId);
        Double r2 = evaluationPersistenceJpa.averageScoreByBusinessId(otherId);

        assertEquals(3.0, r1);
        assertEquals(2.0, r2);
    }

    // -------------------------------------------------------------------------
    //  create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldThrowNotFoundWhenBusinessNotFound() {
        when(businessRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> evaluationPersistenceJpa.create(buildEvaluation(), UUID.randomUUID(), "unknown@test.com"));
        verifyNoInteractions(evaluationRepository);
    }

    @Test
    void create_shouldThrowNotFoundWhenMatchNotFound() {
        BusinessEntity business = buildBusinessEntity("business@test.com");
        when(businessRepository.findByEmail("business@test.com")).thenReturn(Optional.of(business));
        when(matchRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> evaluationPersistenceJpa.create(buildEvaluation(), UUID.randomUUID(), "business@test.com"));
        verifyNoInteractions(evaluationRepository);
    }

    @Test
    void create_shouldThrowForbiddenWhenBusinessDoesNotOwnCampaign() {
        BusinessEntity owner = buildBusinessEntity("owner@test.com");
        BusinessEntity other = buildBusinessEntity("other@test.com");
        MatchEntity match = buildMatchEntity(owner, CampaignStatus.CLOSED);

        when(businessRepository.findByEmail("other@test.com")).thenReturn(Optional.of(other));
        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

        assertThrows(ForbiddenException.class,
                () -> evaluationPersistenceJpa.create(buildEvaluation(), match.getId(), "other@test.com"));
        verifyNoInteractions(evaluationRepository);
    }

    @Test
    void create_shouldThrowConflictWhenCampaignNotClosed() {
        BusinessEntity business = buildBusinessEntity("business@test.com");
        MatchEntity match = buildMatchEntity(business, CampaignStatus.IN_PROGRESS);

        when(businessRepository.findByEmail("business@test.com")).thenReturn(Optional.of(business));
        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

        assertThrows(ConflictException.class,
                () -> evaluationPersistenceJpa.create(buildEvaluation(), match.getId(), "business@test.com"));
        verifyNoInteractions(evaluationRepository);
    }

    @Test
    void create_shouldThrowConflictWhenAlreadyRated() {
        BusinessEntity business = buildBusinessEntity("business@test.com");
        InfluencerEntity influencer = buildInfluencerEntity();
        MatchEntity match = buildMatchEntity(business, CampaignStatus.CLOSED);
        match.setInfluencer(influencer);

        EvaluationEntity existing = EvaluationEntity.builder()
                .id(UUID.randomUUID())
                .score(4)
                .idUserValued(influencer.getId())
                .match(match)
                .build();
        match.setEvaluations(new ArrayList<>(List.of(existing)));

        when(businessRepository.findByEmail("business@test.com")).thenReturn(Optional.of(business));
        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

        assertThrows(ConflictException.class,
                () -> evaluationPersistenceJpa.create(buildEvaluation(), match.getId(), "business@test.com"));
        verifyNoInteractions(evaluationRepository);
    }

    @Test
    void create_shouldSaveAndReturnEvaluationOnSuccess() {
        BusinessEntity   business   = buildBusinessEntity("business@test.com");
        InfluencerEntity influencer = buildInfluencerEntity();
        MatchEntity      match      = buildMatchEntity(business, CampaignStatus.CLOSED);
        match.setInfluencer(influencer);
        match.setEvaluations(new ArrayList<>());

        UUID savedId = UUID.randomUUID();
        when(businessRepository.findByEmail("business@test.com")).thenReturn(Optional.of(business));
        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
        when(evaluationRepository.save(any(EvaluationEntity.class))).thenAnswer(inv -> {
            EvaluationEntity entity = inv.getArgument(0);
            entity.setId(savedId);
            return entity;
        });

        Evaluation result = evaluationPersistenceJpa.create(buildEvaluation(), match.getId(), "business@test.com");

        assertNotNull(result);
        assertEquals(savedId, result.getId());
        assertEquals(5, result.getScore());
        assertEquals(influencer.getId(), result.getValuedUserId());
        assertEquals(match.getId(), result.getMatchId());
    }

    @Test
    void create_shouldSetValuedUserIdToInfluencerId() {
        BusinessEntity business = buildBusinessEntity("business@test.com");
        InfluencerEntity influencer = buildInfluencerEntity();
        MatchEntity match = buildMatchEntity(business, CampaignStatus.CLOSED);
        match.setInfluencer(influencer);
        match.setEvaluations(new ArrayList<>());

        EvaluationEntity saved = EvaluationEntity.builder()
                .id(UUID.randomUUID()).score(3).idUserValued(influencer.getId()).match(match).build();

        when(businessRepository.findByEmail("business@test.com")).thenReturn(Optional.of(business));
        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
        when(evaluationRepository.save(any())).thenReturn(saved);

        evaluationPersistenceJpa.create(Evaluation.builder().score(3).build(), match.getId(), "business@test.com");

        verify(evaluationRepository).save(argThat(e -> influencer.getId().equals(e.getIdUserValued())));
    }

    // ------------------------------------------------------------------------
    //  helpers
    // --------------------------------------------------------------------------

    private Evaluation buildEvaluation() {
        return Evaluation.builder().score(5).build();
    }

    private BusinessEntity buildBusinessEntity(String email) {
        BusinessEntity entity = new BusinessEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmail(email);
        entity.setName("Mi Empresa");
        entity.setPassword("hashedPass");
        return entity;
    }

    private InfluencerEntity buildInfluencerEntity() {
        InfluencerEntity entity = new InfluencerEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmail("influencer@test.com");
        entity.setName("Influencer Test");
        entity.setPassword("hashedPass");
        return entity;
    }

    private MatchEntity buildMatchEntity(BusinessEntity businessOwner, CampaignStatus campaignStatus) {
        CampaignEntity campaign = new CampaignEntity();
        campaign.setId(UUID.randomUUID());
        campaign.setStatus(campaignStatus);
        campaign.setBusiness(businessOwner);

        MatchEntity match = new MatchEntity();
        match.setId(UUID.randomUUID());
        match.setCampaign(campaign);
        match.setEvaluations(new ArrayList<>());
        return match;
    }
}
