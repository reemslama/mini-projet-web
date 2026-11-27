package tn.arene.ms1challenges.services;

import org.springframework.data.domain.Page;
import tn.arene.ms1challenges.dto.AcceptanceCriterionDto;
import tn.arene.ms1challenges.dto.ChallengeCreateRequest;
import tn.arene.ms1challenges.dto.ChallengeResponse;
import tn.arene.ms1challenges.dto.PrizeDto;
import tn.arene.ms1challenges.entities.ChallengeStatus;

import java.util.List;
import java.util.UUID;

public interface IChallengeService {

    ChallengeResponse createChallenge(ChallengeCreateRequest request);

    Page<ChallengeResponse> searchChallenges(String tag, ChallengeStatus status, Double minPrize, int page, int size);

    ChallengeResponse getChallengeById(UUID id);

    ChallengeResponse updateChallenge(UUID id, ChallengeCreateRequest request);

    ChallengeResponse publishChallenge(UUID id);

    List<AcceptanceCriterionDto> getAcceptanceCriteria(UUID id);

    List<AcceptanceCriterionDto> updateAcceptanceCriteria(UUID id, List<AcceptanceCriterionDto> criteria);

    List<PrizeDto> getPrizes(UUID id);

    List<PrizeDto> updatePrizes(UUID id, List<PrizeDto> prizes);
}
