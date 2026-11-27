package tn.arene.reputation.services;

import tn.arene.reputation.dto.BadgeRequest;
import tn.arene.reputation.dto.ProfileRequest;
import tn.arene.reputation.dto.ReputationEventRequest;
import tn.arene.reputation.dto.Responses.*;

import java.util.List;
import java.util.UUID;

public interface IReputationService {

    // CRUD profils
    List<ProfileResponse> findAllProfiles();
    ProfileResponse findProfile(Long id);
    ProfileResponse findByDeveloper(UUID developerId);
    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse updateProfile(Long id, ProfileRequest request);
    void deleteProfile(Long id);

    // Badges
    List<BadgeResponse> findBadges(UUID developerId);
    BadgeResponse awardBadge(UUID developerId, BadgeRequest request);
    void revokeBadge(Long badgeId);

    // Evenements
    /** @return true si applique, false si doublon ignore (idempotence). */
    boolean ingest(ReputationEventRequest request);
    List<EventResponse> findEvents(UUID developerId);
    void deleteEvent(Long eventId);

    // Classement
    List<RankingEntry> ranking(String skill, int page, int size);
}
