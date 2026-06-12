package org.nck.repository;

import org.nck.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByPuuid(String puuid);
    Optional<Player> findByNameAndTag(String name, String tag);
    boolean existsByPuuid(String puuid);
}
