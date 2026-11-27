package tn.arene.reputation.dto;

import tn.arene.reputation.entities.Badge;
import tn.arene.reputation.entities.ReputationEvent;
import tn.arene.reputation.entities.ReputationEventType;
import tn.arene.reputation.entities.ReputationProfile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Reponses JSON (schemas du contrat). */
public final class Responses {

    private Responses() {
    }

    public record ProfileResponse(Long id, UUID developerId, String displayName, int score,
                                  int challengesWon, int submissionsPassed, List<String> skills,
                                  List<BadgeResponse> badges, LocalDateTime updatedAt) {
        public static ProfileResponse of(ReputationProfile p) {
            return new ProfileResponse(p.getId(), p.getDeveloperId(), p.getDisplayName(), p.getScore(),
                    p.getChallengesWon(), p.getSubmissionsPassed(), List.copyOf(p.getSkills()),
                    p.getBadges().stream().map(BadgeResponse::of).toList(), p.getUpdatedAt());
        }
    }

    public record BadgeResponse(Long id, String code, String label, LocalDateTime awardedAt) {
        public static BadgeResponse of(Badge b) {
            return new BadgeResponse(b.getId(), b.getCode(), b.getLabel(), b.getAwardedAt());
        }
    }

    public record EventResponse(Long id, ReputationEventType type, UUID developerId, UUID challengeId,
                                int weight, LocalDateTime occurredAt) {
        public static EventResponse of(ReputationEvent e) {
            return new EventResponse(e.getId(), e.getType(), e.getDeveloperId(), e.getChallengeId(),
                    e.getWeight(), e.getOccurredAt());
        }
    }

    public record RankingEntry(int rank, UUID developerId, String displayName, int score) {
    }
}
