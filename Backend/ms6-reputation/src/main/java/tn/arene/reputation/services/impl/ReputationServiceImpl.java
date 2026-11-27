package tn.arene.reputation.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.arene.reputation.dto.BadgeRequest;
import tn.arene.reputation.dto.ProfileRequest;
import tn.arene.reputation.dto.ReputationEventRequest;
import tn.arene.reputation.dto.Responses.*;
import tn.arene.reputation.entities.Badge;
import tn.arene.reputation.entities.ReputationEvent;
import tn.arene.reputation.entities.ReputationProfile;
import tn.arene.reputation.exceptions.GlobalExceptionHandler.ConflictException;
import tn.arene.reputation.exceptions.GlobalExceptionHandler.NotFoundException;
import tn.arene.reputation.repositories.BadgeRepository;
import tn.arene.reputation.repositories.ReputationEventRepository;
import tn.arene.reputation.repositories.ReputationProfileRepository;
import tn.arene.reputation.services.IReputationService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReputationServiceImpl implements IReputationService {

    private final ReputationProfileRepository profiles;
    private final BadgeRepository badges;
    private final ReputationEventRepository events;

    // ------------------------------------------------------------------ profils

    @Override
    @Transactional(readOnly = true)
    public List<ProfileResponse> findAllProfiles() {
        return profiles.findAll().stream().map(ProfileResponse::of).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse findProfile(Long id) {
        return ProfileResponse.of(getProfile(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse findByDeveloper(UUID developerId) {
        return ProfileResponse.of(getByDeveloper(developerId));
    }

    @Override
    public ProfileResponse createProfile(ProfileRequest req) {
        if (profiles.existsByDeveloperId(req.developerId())) {
            throw new ConflictException("Le developpeur " + req.developerId() + " a deja un profil");
        }
        ReputationProfile p = ReputationProfile.of(req.developerId(), req.displayName());
        if (req.skills() != null) p.setSkills(new ArrayList<>(req.skills()));
        return ProfileResponse.of(profiles.save(p));
    }

    @Override
    public ProfileResponse updateProfile(Long id, ProfileRequest req) {
        ReputationProfile p = getProfile(id);
        if (!p.getDeveloperId().equals(req.developerId()) && profiles.existsByDeveloperId(req.developerId())) {
            throw new ConflictException("Le developpeur " + req.developerId() + " a deja un profil");
        }
        p.setDeveloperId(req.developerId());
        p.setDisplayName(req.displayName());
        if (req.skills() != null) {
            p.getSkills().clear();
            p.getSkills().addAll(req.skills());
        }
        return ProfileResponse.of(p);
    }

    @Override
    public void deleteProfile(Long id) {
        profiles.delete(getProfile(id));
    }

    // ------------------------------------------------------------------- badges

    @Override
    @Transactional(readOnly = true)
    public List<BadgeResponse> findBadges(UUID developerId) {
        return getByDeveloper(developerId).getBadges().stream().map(BadgeResponse::of).toList();
    }

    @Override
    public BadgeResponse awardBadge(UUID developerId, BadgeRequest req) {
        ReputationProfile p = getByDeveloper(developerId);
        if (p.hasBadge(req.code())) {
            throw new ConflictException("Badge " + req.code() + " deja attribue");
        }
        Badge b = Badge.award(req.code().toUpperCase(), req.label());
        p.addBadge(b);
        profiles.flush();
        return BadgeResponse.of(b);
    }

    @Override
    public void revokeBadge(Long badgeId) {
        Badge b = badges.findById(badgeId).orElseThrow(() -> new NotFoundException("Badge " + badgeId + " introuvable"));
        b.getProfile().getBadges().remove(b);
    }

    // -------------------------------------------------------------- evenements

    @Override
    public boolean ingest(ReputationEventRequest req) {
        if (events.existsByTypeAndDeveloperIdAndChallengeId(req.type(), req.developerId(), req.challengeId())) {
            return false; // doublon : deja compte une fois
        }
        ReputationProfile p = profiles.findByDeveloperId(req.developerId())
                .orElseGet(() -> profiles.save(ReputationProfile.of(req.developerId(),
                        "dev-" + req.developerId().toString().substring(0, 8))));
        p.apply(ReputationEvent.of(req.type(), req.developerId(), req.challengeId(), req.weight(), req.occurredAt()));
        BadgeRules.applyTo(p);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> findEvents(UUID developerId) {
        return getByDeveloper(developerId).getEvents().stream().map(EventResponse::of).toList();
    }

    @Override
    public void deleteEvent(Long eventId) {
        ReputationEvent e = events.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evenement " + eventId + " introuvable"));
        ReputationProfile p = e.getProfile();
        p.getEvents().remove(e);
        p.recompute();
    }

    // -------------------------------------------------------------- classement

    @Override
    @Transactional(readOnly = true)
    public List<RankingEntry> ranking(String skill, int page, int size) {
        PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<ReputationProfile> result = (skill == null || skill.isBlank())
                ? profiles.findAllByOrderByScoreDescUpdatedAtAsc(pr)
                : profiles.findBySkill(skill, pr);
        List<RankingEntry> out = new ArrayList<>();
        int rank = (int) pr.getOffset();
        for (ReputationProfile p : result) {
            out.add(new RankingEntry(++rank, p.getDeveloperId(), p.getDisplayName(), p.getScore()));
        }
        return out;
    }

    // ----------------------------------------------------------------- helpers

    private ReputationProfile getProfile(Long id) {
        return profiles.findById(id).orElseThrow(() -> new NotFoundException("Profil " + id + " introuvable"));
    }

    private ReputationProfile getByDeveloper(UUID developerId) {
        return profiles.findByDeveloperId(developerId)
                .orElseThrow(() -> new NotFoundException("Aucun profil pour le developpeur " + developerId));
    }
}
