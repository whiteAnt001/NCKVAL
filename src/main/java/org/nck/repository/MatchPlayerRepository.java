package org.nck.repository;

import org.nck.entity.Match;
import org.nck.entity.MatchPlayer;
import org.nck.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, Long> {
    List<MatchPlayer> findByPlayer(Player player);
    List<MatchPlayer> findByPlayerAndIsWin(Player player, boolean isWin);
    int countByPlayer(Player player);
    int countByPlayerAndIsWin(Player player, boolean isWin);
    boolean existsByMatchAndPlayer(Match match, Player player);
}
