package tn.arene.reputation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.arene.reputation.entities.Badge;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
}
