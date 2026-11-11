package com.enlice.challenges.service;

import com.enlice.challenges.domain.AcceptanceCriterion;
import com.enlice.challenges.domain.AcceptanceCriterionRepository;
import com.enlice.challenges.domain.Challenge;
import com.enlice.challenges.domain.ChallengeRepository;
import com.enlice.challenges.domain.ChallengeStatus;
import com.enlice.challenges.domain.Prize;
import com.enlice.challenges.domain.PrizeRepository;
import com.enlice.challenges.dto.AcceptanceCriterionDto;
import com.enlice.challenges.dto.ChallengeCreateRequest;
import com.enlice.challenges.dto.ChallengeResponse;
import com.enlice.challenges.dto.PrizeDto;
import com.enlice.challenges.events.ChallengePublishedPayload;
import com.enlice.challenges.events.EventPublisher;
import com.enlice.challenges.web.ConflictException;
import com.enlice.challenges.web.NotFoundException;
import com.enlice.challenges.web.PageResponse;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChallengeService {

    private static final String PUBLISH_BINDING = "challengePublished-out-0";

    private final ChallengeRepository challengeRepository;
    private final AcceptanceCriterionRepository criterionRepository;
    private final PrizeRepository prizeRepository;
    private final EventPublisher eventPublisher;

    public ChallengeService(ChallengeRepository challengeRepository,
                            AcceptanceCriterionRepository criterionRepository,
                            PrizeRepository prizeRepository,
                            EventPublisher eventPublisher) {
        this.challengeRepository = challengeRepository;
        this.criterionRepository = criterionRepository;
        this.prizeRepository = prizeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ChallengeResponse create(ChallengeCreateRequest request) {
        Challenge challenge = new Challenge(UUID.randomUUID(), request.title(), request.description(),
                request.enterpriseId(), request.deadline(), normalizeTags(request.tags()), Instant.now());
        return ChallengeResponse.from(challengeRepository.save(challenge));
    }

    @Transactional(readOnly = true)
    public ChallengeResponse get(UUID id) {
        return ChallengeResponse.from(requireChallenge(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ChallengeResponse> list(String tag, ChallengeStatus status, Double minPrize,
                                                Pageable pageable) {
        Specification<Challenge> spec = (root, query, cb) -> cb.conjunction();
        if (tag != null && !tag.isBlank()) {
            String normalized = tag.trim();
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Challenge, String> tags = root.join("tags", JoinType.INNER);
                return cb.equal(cb.lower(tags), normalized.toLowerCase());
            });
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (minPrize != null) {
            spec = spec.and((root, query, cb) -> {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Prize> prize = subquery.from(Prize.class);
                subquery.select(cb.count(prize));
                subquery.where(cb.equal(prize.get("challengeId"), root.get("id")),
                        cb.greaterThanOrEqualTo(prize.get("amount"), minPrize));
                return cb.greaterThan(subquery, 0L);
            });
        }
        Page<ChallengeResponse> page = challengeRepository.findAll(spec, pageable).map(ChallengeResponse::from);
        return PageResponse.from(page);
    }

    @Transactional
    public ChallengeResponse update(UUID id, ChallengeCreateRequest request) {
        Challenge challenge = requireChallenge(id);
        if (challenge.getStatus() != ChallengeStatus.DRAFT) {
            throw new ConflictException("Un défi n'est modifiable que lorsqu'il est en brouillon (statut actuel : "
                    + challenge.getStatus() + ")");
        }
        challenge.setTitle(request.title());
        challenge.setDescription(request.description());
        challenge.setEnterpriseId(request.enterpriseId());
        challenge.setDeadline(request.deadline());
        challenge.setTags(normalizeTags(request.tags()));
        return ChallengeResponse.from(challengeRepository.save(challenge));
    }

    @Transactional
    public ChallengeResponse publish(UUID id) {
        Challenge challenge = requireChallenge(id);
        long criteriaCount = criterionRepository.countByChallengeId(id);
        long prizeCount = prizeRepository.countByChallengeId(id);
        PublishValidator.validate(challenge.getStatus(), criteriaCount, prizeCount, challenge.getDeadline(),
                Instant.now());
        challenge.setStatus(ChallengeStatus.PUBLISHED);
        challenge.setPublishedAt(Instant.now());
        Challenge saved = challengeRepository.save(challenge);

        // Publication uniquement après persistance de l'état métier.
        eventPublisher.publish(PUBLISH_BINDING, "challenge.published", new ChallengePublishedPayload(
                saved.getId(), saved.getEnterpriseId(), saved.getDeadline(), new LinkedHashSet<>(saved.getTags())));
        return ChallengeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AcceptanceCriterionDto> getCriteria(UUID challengeId) {
        requireChallenge(challengeId);
        return criterionRepository.findByChallengeIdOrderByWeightDesc(challengeId).stream()
                .map(criterion -> new AcceptanceCriterionDto(criterion.getId(), criterion.getDescription(),
                        BigDecimal.valueOf(criterion.getWeight()), criterion.getTestCommand()))
                .toList();
    }

    @Transactional
    public List<AcceptanceCriterionDto> replaceCriteria(UUID challengeId, List<AcceptanceCriterionDto> criteria) {
        requireChallenge(challengeId);
        criterionRepository.deleteByChallengeId(challengeId);
        criterionRepository.flush();
        Set<String> usedIds = new HashSet<>();
        List<AcceptanceCriterion> entities = new ArrayList<>();
        for (AcceptanceCriterionDto dto : criteria) {
            String criterionId = dto.id();
            if (criterionId == null || criterionId.isBlank() || !usedIds.add(criterionId)) {
                criterionId = UUID.randomUUID().toString();
                usedIds.add(criterionId);
            }
            entities.add(new AcceptanceCriterion(criterionId, challengeId, dto.description(),
                    dto.weight() == null ? 0.0 : dto.weight().doubleValue(), dto.testCommand()));
        }
        return criterionRepository.saveAll(entities).stream()
                .map(criterion -> new AcceptanceCriterionDto(criterion.getId(), criterion.getDescription(),
                        BigDecimal.valueOf(criterion.getWeight()), criterion.getTestCommand()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrizeDto> getPrizes(UUID challengeId) {
        requireChallenge(challengeId);
        return prizeRepository.findByChallengeIdOrderByRankAsc(challengeId).stream()
                .map(prize -> new PrizeDto(prize.getRank(), BigDecimal.valueOf(prize.getAmount()), prize.getCurrency()))
                .toList();
    }

    @Transactional
    public List<PrizeDto> replacePrizes(UUID challengeId, List<PrizeDto> prizes) {
        requireChallenge(challengeId);
        Set<Integer> ranks = new HashSet<>();
        for (PrizeDto prize : prizes) {
            if (prize.rank() == null || prize.rank() < 1 || prize.rank() > 3) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le rang d'une prime doit être compris entre 1 et 3");
            }
            if (!ranks.add(prize.rank())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les rangs des primes doivent être uniques");
            }
            if (prize.amount() == null || prize.amount().signum() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le montant d'une prime doit être positif ou nul");
            }
        }
        prizeRepository.deleteByChallengeId(challengeId);
        prizeRepository.flush();
        List<Prize> entities = prizes.stream()
                .map(prize -> new Prize(UUID.randomUUID(), challengeId, prize.rank(), prize.amount().doubleValue(),
                        prize.currency()))
                .toList();
        return prizeRepository.saveAll(entities).stream()
                .map(prize -> new PrizeDto(prize.getRank(), BigDecimal.valueOf(prize.getAmount()), prize.getCurrency()))
                .toList();
    }

    private Challenge requireChallenge(UUID id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Défi introuvable : " + id));
    }

    private Set<String> normalizeTags(Set<String> tags) {
        if (tags == null) {
            return new LinkedHashSet<>();
        }
        Set<String> normalized = new LinkedHashSet<>();
        tags.stream().filter(tag -> tag != null && !tag.isBlank()).map(String::trim).forEach(normalized::add);
        return normalized;
    }
}
