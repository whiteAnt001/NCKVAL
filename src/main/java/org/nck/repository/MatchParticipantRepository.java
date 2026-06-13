package org.nck.repository;

import org.nck.entity.Match;
import org.nck.entity.MatchParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, Long> {
    List<MatchParticipant> findByMatch(Match match);
    List<MatchParticipant> findByMatchOrderByTeamAscKillsDesc(Match match);
    boolean existsByMatchAndPuuid(Match match, String puuid);
    void deleteByMatch(Match match);
}
