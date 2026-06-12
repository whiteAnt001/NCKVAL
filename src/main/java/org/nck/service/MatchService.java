package org.nck.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.nck.config.HenrikApiClient;
import org.nck.entity.Match;
import org.nck.entity.MatchPlayer;
import org.nck.entity.Player;
import org.nck.repository.MatchPlayerRepository;
import org.nck.repository.MatchRepository;
import org.nck.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {
    private final MatchRepository matchRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final PlayerRepository playerRepository;
    private final HenrikApiClient henrikApiClient;

    public int syncMatches(String name, String tag) {

        Player player = playerRepository.findByNameAndTag(name, tag)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 플레이어입니다."));

        JsonNode matches = henrikApiClient.getMatchHistory(player.getRegion(), name, tag);

        if (matches == null || !matches.isArray()) return 0;

        int saved = 0;

        for (JsonNode matchNode : matches) {
            String matchId = matchNode.path("metadata").path("matchid").asText();

            String map = matchNode.path("metadata").path("map").asText();
            int roundsPlayed = matchNode.path("metadata").path("rounds_played").asInt();

            JsonNode teams = matchNode.path("teams");
            final String finalWinningTeam;
            if (teams.path("red").path("has_won").asBoolean()) finalWinningTeam = "Red";
            else if (teams.path("blue").path("has_won").asBoolean()) finalWinningTeam = "Blue";
            else finalWinningTeam = "Unknown";

            // 매치가 없으면 새로 저장, 있으면 기존 매치 가져오기
            final Match match;
            if (!matchRepository.existsByMatchId(matchId)) {
                Match newMatch = Match.builder()
                        .matchId(matchId)
                        .map(map)
                        .winningTeam(finalWinningTeam)
                        .roundsPlayed(roundsPlayed)
                        .build();
                match = matchRepository.save(newMatch);
                saved++;
            } else {
                match = matchRepository.findByMatchId(matchId).get();
            }

            // 플레이어 저장은 항상 실행 (나중에 등록된 플레이어도 반영)
            JsonNode players = matchNode.path("players").path("all_players");
            for (JsonNode p : players) {
                String puuid = p.path("puuid").asText();
                playerRepository.findByPuuid(puuid).ifPresent(registeredPlayer -> {
                    // 이미 저장된 match_player면 스킵
                    if (matchPlayerRepository.existsByMatchAndPlayer(match, registeredPlayer)) return;

                    String team = p.path("team").asText();
                    String agent = p.path("character").asText();
                    int kills = p.path("stats").path("kills").asInt();
                    int deaths = p.path("stats").path("deaths").asInt();
                    int assists = p.path("stats").path("assists").asInt();
                    int score = p.path("stats").path("score").asInt();
                    boolean isWin = team.equalsIgnoreCase(finalWinningTeam);

                    MatchPlayer matchPlayer = MatchPlayer.builder()
                            .match(match)
                            .player(registeredPlayer)
                            .team(team)
                            .agent(agent)
                            .kills(kills)
                            .deaths(deaths)
                            .assists(assists)
                            .score(score)
                            .isWin(isWin)
                            .build();

                    matchPlayerRepository.save(matchPlayer);
                });
            }
        }
        return saved;
    }
}
