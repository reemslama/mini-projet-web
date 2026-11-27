package tn.arene.ms1challenges.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.arene.ms1challenges.dto.AcceptanceCriterionDto;
import tn.arene.ms1challenges.dto.ChallengeCreateRequest;
import tn.arene.ms1challenges.dto.ChallengeResponse;
import tn.arene.ms1challenges.dto.PrizeDto;
import tn.arene.ms1challenges.entities.AcceptanceCriterion;
import tn.arene.ms1challenges.entities.Challenge;
import tn.arene.ms1challenges.entities.ChallengeStatus;
import tn.arene.ms1challenges.entities.Prize;
import tn.arene.ms1challenges.exceptions.BadRequestException;
import tn.arene.ms1challenges.exceptions.ConflictException;
import tn.arene.ms1challenges.exceptions.ResourceNotFoundException;
import tn.arene.ms1challenges.repositories.ChallengeRepository;
import tn.arene.ms1challenges.services.IChallengeService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeServiceImpl implements IChallengeService {

    private final ChallengeRepository challengeRepository;

    @Override
    @Transactional
    public ChallengeResponse createChallenge(ChallengeCreateRequest request) {
        if (request.getTitle() != null && request.getTitle().length() > 140) {
            throw new BadRequestException("Le titre du défi ne doit pas dépasser 140 caractères.");
        }

        Challenge challenge = Challenge.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .enterpriseId(request.getEnterpriseId())
                .deadline(request.getDeadline())
                .tags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>())
                .status(ChallengeStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .publishedAt(null)
                .acceptanceCriteria(new ArrayList<>())
                .prizes(new ArrayList<>())
                .build();

        Challenge saved = challengeRepository.save(challenge);
        log.info("Nouveau défi créé en statut DRAFT : id={}", saved.getId());
        return ChallengeResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChallengeResponse> searchChallenges(String tag, ChallengeStatus status, Double minPrize, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Challenge> challenges;

        if (tag == null && status == null && minPrize == null) {
            challenges = challengeRepository.findAll(pageable);
        } else {
            challenges = challengeRepository.findWithFilters(status, tag, minPrize, pageable);
        }

        return challenges.map(ChallengeResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ChallengeResponse getChallengeById(UUID id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Défi non trouvé avec l'id : " + id));
        return ChallengeResponse.fromEntity(challenge);
    }

    @Override
    @Transactional
    public ChallengeResponse updateChallenge(UUID id, ChallengeCreateRequest request) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Défi non trouvé avec l'id : " + id));

        if (challenge.getStatus() != ChallengeStatus.DRAFT) {
            throw new ConflictException("Modification impossible : le défi est en statut " + challenge.getStatus() +
                    ". Seuls les défis en statut DRAFT peuvent être modifiés.");
        }

        if (request.getTitle() != null && request.getTitle().length() > 140) {
            throw new BadRequestException("Le titre du défi ne doit pas dépasser 140 caractères.");
        }

        challenge.update(request.getTitle(), request.getDescription(), request.getDeadline(), request.getTags());
        Challenge updated = challengeRepository.save(challenge);
        log.info("Défi mis à jour : id={}", updated.getId());
        return ChallengeResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public ChallengeResponse publishChallenge(UUID id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Défi non trouvé avec l'id : " + id));

        if (challenge.getStatus() != ChallengeStatus.DRAFT) {
            throw new ConflictException("Publication impossible : le défi est déjà publié ou annulé (statut=" + challenge.getStatus() + ").");
        }

        challenge.publish();
        Challenge published = challengeRepository.save(challenge);
        log.info("Défi publié : id={}, publishedAt={}", published.getId(), published.getPublishedAt());
        // Note: Événement RabbitMQ 'challenge.published' déclenché ici
        return ChallengeResponse.fromEntity(published);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcceptanceCriterionDto> getAcceptanceCriteria(UUID id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Défi non trouvé avec l'id : " + id));

        return challenge.getAcceptanceCriteria().stream()
                .map(AcceptanceCriterionDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AcceptanceCriterionDto> updateAcceptanceCriteria(UUID id, List<AcceptanceCriterionDto> criteria) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Défi non trouvé avec l'id : " + id));

        List<AcceptanceCriterion> entityList = new ArrayList<>();
        if (criteria != null) {
            for (AcceptanceCriterionDto dto : criteria) {
                if (dto.getWeight() != null && (dto.getWeight() < 0.0 || dto.getWeight() > 1.0)) {
                    throw new BadRequestException("Le poids du critère '" + dto.getId() + "' doit être compris entre 0.0 et 1.0.");
                }
                entityList.add(dto.toEntity());
            }
        }

        challenge.getAcceptanceCriteria().clear();
        challenge.getAcceptanceCriteria().addAll(entityList);
        Challenge saved = challengeRepository.save(challenge);
        log.info("Critères d'acceptation mis à jour pour le défi id={}", saved.getId());

        return saved.getAcceptanceCriteria().stream()
                .map(AcceptanceCriterionDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrizeDto> getPrizes(UUID id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Défi non trouvé avec l'id : " + id));

        return challenge.getPrizes().stream()
                .map(PrizeDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PrizeDto> updatePrizes(UUID id, List<PrizeDto> prizes) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Défi non trouvé avec l'id : " + id));

        List<Prize> prizeList = new ArrayList<>();
        if (prizes != null) {
            for (PrizeDto dto : prizes) {
                if (dto.getRank() == null || dto.getRank() < 1 || dto.getRank() > 3) {
                    throw new BadRequestException("Le rang de la prime doit être compris entre 1 et 3 (Top 3).");
                }
                prizeList.add(dto.toEntity());
            }
        }

        challenge.setPrizesList(prizeList);
        Challenge saved = challengeRepository.save(challenge);
        log.info("Primes mises à jour pour le défi id={}", saved.getId());

        return saved.getPrizes().stream()
                .map(PrizeDto::fromEntity)
                .collect(Collectors.toList());
    }
}
