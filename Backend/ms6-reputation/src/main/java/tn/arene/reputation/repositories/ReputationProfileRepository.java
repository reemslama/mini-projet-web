package tn.arene.reputation.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.arene.reputation.entities.ReputationProfile;

import java.util.Optional;
import java.util.UUID;

public interface ReputationProfileRepository extends JpaRepository<ReputationProfile, Long> {

    Optional<ReputationProfile> findByDeveloperId(UUID developerId);

    boolean existsByDeveloperId(UUID developerId);

    /** Classement calcule a la demande (read model, rien n'est stocke). */
    Page<ReputationProfile> findAllByOrderByScoreDescUpdatedAtAsc(Pageable pageable);

    @Query(value = "select p from ReputationProfile p join p.skills s where lower(s) = lower(:skill) order by p.score desc, p.updatedAt asc",
           countQuery = "select count(p) from ReputationProfile p join p.skills s where lower(s) = lower(:skill)")
    Page<ReputationProfile> findBySkill(@Param("skill") String skill, Pageable pageable);
}
