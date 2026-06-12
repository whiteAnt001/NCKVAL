package org.nck.repository;

import org.nck.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    boolean existsByMatchId(String matchId);
    Optional<Match> findByMatchId(String matchId);
}
