package org.nck.repository;

import org.nck.entity.Match;
import org.nck.entity.MatchRound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRoundRepository extends JpaRepository<MatchRound, Long> {
    List<MatchRound> findByMatchOrderByRoundNumberAsc(Match match);
    void deleteByMatch(Match match);
}
