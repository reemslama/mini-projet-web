package tn.arene.ms1challenges.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.arene.ms1challenges.entities.Challenge;
import tn.arene.ms1challenges.entities.ChallengeStatus;

import java.util.UUID;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, UUID>, JpaSpecificationExecutor<Challenge> {

    @Query("SELECT DISTINCT c FROM Challenge c LEFT JOIN c.tags t LEFT JOIN c.prizes p WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:tag IS NULL OR :tag = '' OR LOWER(t) = LOWER(:tag)) AND " +
           "(:minPrize IS NULL OR p.amount >= :minPrize)")
    Page<Challenge> findWithFilters(
            @Param("status") ChallengeStatus status,
            @Param("tag") String tag,
            @Param("minPrize") Double minPrize,
            Pageable pageable
    );
}
