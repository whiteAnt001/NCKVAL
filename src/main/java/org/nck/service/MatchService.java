package org.nck.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.nck.config.HenrikApiClient;
import org.nck.entity.*;
import org.nck.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {
    private final MatchRepository matchRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final MatchRoundRepository matchRoundRepository;
    private final PlayerRepository playerRepository;
    private final HenrikApiClient henrikApiClient;

    public int syncAllMatches() {
        List<Player> players = playerRepository.findAll();

        matchPlayerRepository.deleteAll();
        matchParticipantRepository.deleteAll();
        matchRoundRepository.deleteAll();
        matchRepository.deleteAll();

        int saved = 0;
        for (Player player : players) {
            try {
                saved += syncMatches(player.getName(), player.getTag());
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("동기화 실패: " + player.getName() + " - " + e.getMessage());
            }
        }
        return saved;
    }

    public int syncMatches(String name, String tag) {

        Player player = playerRepository.findByNameAndTag(name, tag)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 플레이어입니다."));

        JsonNode matches = henrikApiClient.getMatchHistory(player.getRegion(), name, tag);

        if (matches == null || !matches.isArray()) return 0;

        // 등록된 플레이어 puuid 캐싱 (매번 DB 조회 안 하도록)
        List<Player> allPlayers = playerRepository.findAll();
        java.util.Map<String, Player> puuidToPlayer = allPlayers.stream()
                .collect(java.util.stream.Collectors.toMap(Player::getPuuid, p -> p));

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

            final Match match;
            if (!matchRepository.existsByMatchId(matchId)) {
                Match newMatch = Match.builder()
                        .matchId(matchId)
                        .map(map)
                        .winningTeam(finalWinningTeam)
                        .roundsPlayed(roundsPlayed)
                        .playedAt(java.time.LocalDateTime.ofEpochSecond(
                                matchNode.path("metadata").path("game_start").asLong(),
                                0,
                                java.time.ZoneOffset.ofHours(9)))
                        .build();
                match = matchRepository.save(newMatch);
                saved++;

                // 라운드 saveAll
                JsonNode rounds = matchNode.path("rounds");
                if (rounds.isArray()) {
                    List<MatchRound> roundList = new java.util.ArrayList<>();
                    for (int i = 0; i < rounds.size(); i++) {
                        JsonNode round = rounds.get(i);
                        roundList.add(MatchRound.builder()
                                .match(match)
                                .roundNumber(i + 1)
                                .winningTeam(round.path("winning_team").asText())
                                .winCondition(round.path("end_type").asText())
                                .build());
                    }
                    matchRoundRepository.saveAll(roundList);
                }

                // 참여자 saveAll
                JsonNode allMatchPlayers = matchNode.path("players").path("all_players");
                List<MatchParticipant> participantList = new java.util.ArrayList<>();
                List<MatchPlayer> matchPlayerList = new java.util.ArrayList<>();

                for (JsonNode p : allMatchPlayers) {
                    String puuid = p.path("puuid").asText();
                    String team = p.path("team").asText();
                    boolean isWin = team.equalsIgnoreCase(finalWinningTeam);
                    int kills = p.path("stats").path("kills").asInt();
                    int deaths = p.path("stats").path("deaths").asInt();
                    int assists = p.path("stats").path("assists").asInt();
                    int score = p.path("stats").path("score").asInt();

                    // 전체 참여자
                    participantList.add(MatchParticipant.builder()
                            .match(match)
                            .puuid(puuid)
                            .displayName(p.path("name").asText() + "#" + p.path("tag").asText())
                            .team(team)
                            .agent(p.path("character").asText())
                            .kills(kills)
                            .deaths(deaths)
                            .assists(assists)
                            .score(score)
                            .isWin(isWin)
                            .build());

                    // 등록된 플레이어만 match_players
                    if (puuidToPlayer.containsKey(puuid)) {
                        matchPlayerList.add(MatchPlayer.builder()
                                .match(match)
                                .player(puuidToPlayer.get(puuid))
                                .team(team)
                                .agent(p.path("character").asText())
                                .kills(kills)
                                .deaths(deaths)
                                .assists(assists)
                                .score(score)
                                .isWin(isWin)
                                .build());
                    }
                }

                matchParticipantRepository.saveAll(participantList);
                matchPlayerRepository.saveAll(matchPlayerList);

            } else {
                match = matchRepository.findByMatchId(matchId).get();

                // 기존 매치도 등록된 플레이어 누락분 체크
                JsonNode allMatchPlayers = matchNode.path("players").path("all_players");
                List<MatchPlayer> matchPlayerList = new java.util.ArrayList<>();

                for (JsonNode p : allMatchPlayers) {
                    String puuid = p.path("puuid").asText();
                    if (!puuidToPlayer.containsKey(puuid)) continue;
                    Player registeredPlayer = puuidToPlayer.get(puuid);
                    if (matchPlayerRepository.existsByMatchAndPlayer(match, registeredPlayer)) continue;

                    String team = p.path("team").asText();
                    boolean isWin = team.equalsIgnoreCase(finalWinningTeam);

                    matchPlayerList.add(MatchPlayer.builder()
                            .match(match)
                            .player(registeredPlayer)
                            .team(team)
                            .agent(p.path("character").asText())
                            .kills(p.path("stats").path("kills").asInt())
                            .deaths(p.path("stats").path("deaths").asInt())
                            .assists(p.path("stats").path("assists").asInt())
                            .score(p.path("stats").path("score").asInt())
                            .isWin(isWin)
                            .build());
                }
                matchPlayerRepository.saveAll(matchPlayerList);
            }
        }
        return saved;
    }
}