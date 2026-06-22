package org.nck.service;

import lombok.RequiredArgsConstructor;
import org.nck.dto.AgentStatsDto;
import org.nck.dto.MatchDetailDto;
import org.nck.dto.MatchHistoryDto;
import org.nck.dto.StatsResponseDto;
import org.nck.entity.Match;
import org.nck.entity.MatchPlayer;
import org.nck.entity.Player;
import org.nck.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final PlayerRepository playerRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final MatchRepository matchRepository;
    private final MatchRoundRepository matchRoundRepository;
    private final MatchParticipantRepository matchParticipantRepository;

    public StatsResponseDto getStats(String name, String tag) {
        Player player = playerRepository.findByNameAndTag(name, tag)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 플레이어 입니다."));

        List<MatchPlayer> matchPlayers = matchPlayerRepository.findByPlayer(player);

        int totalGames = matchPlayers.size();
        int wins = (int) matchPlayers.stream().filter(MatchPlayer::isWin).count();
        int losses = totalGames - wins;
        double winRate = totalGames == 0 ? 0.0 : (double) wins / totalGames * 100;

        int totalKills = matchPlayers.stream().mapToInt(MatchPlayer::getKills).sum();
        int totalDeaths = matchPlayers.stream().mapToInt(MatchPlayer::getDeaths).sum();
        int totalAssists = matchPlayers.stream().mapToInt(MatchPlayer::getAssists).sum();

        // KDA → KD로 변경
        double kd = totalDeaths == 0 ? totalKills : (double) totalKills / totalDeaths;

        Map<String, Long> agentPickCount = matchPlayers.stream()
                .collect(Collectors.groupingBy(MatchPlayer::getAgent, Collectors.counting()));

        Map<String, Long> agentWinCount = matchPlayers.stream()
                .filter(MatchPlayer::isWin)
                .collect(Collectors.groupingBy(MatchPlayer::getAgent, Collectors.counting()));

        List<AgentStatsDto> agentStats = agentPickCount.entrySet().stream()
                .map(entry -> {
                    String agent = entry.getKey();
                    long picks = entry.getValue();
                    long agentWins = agentWinCount.getOrDefault(agent, 0L);
                    double agentWinRate = (double) agentWins / picks * 100;
                    return AgentStatsDto.builder()
                            .agent(agent)
                            .picks((int) picks)
                            .wins((int) agentWins)
                            .winRate(Math.round(agentWinRate * 10) / 10.0)
                            .build();
                })
                .sorted(Comparator.comparingInt(AgentStatsDto::getPicks).reversed())
                .collect(Collectors.toList());

        List<MatchHistoryDto> recentMatches = matchPlayers.stream()
                .sorted(Comparator.comparing(mp -> mp.getMatch().getPlayedAt(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(mp -> {
                    List<String> registeredPlayers = matchPlayerRepository.findByMatch(mp.getMatch())
                            .stream()
                            .map(rp -> rp.getPlayer().getName())
                            .collect(Collectors.toList());

                    return MatchHistoryDto.builder()
                            .matchId(mp.getMatch().getMatchId())
                            .map(mp.getMatch().getMap())
                            .winningTeam(mp.getMatch().getWinningTeam())
                            .roundsPlayed(mp.getMatch().getRoundsPlayed())
                            .playedAt(mp.getMatch().getPlayedAt())
                            .agent(mp.getAgent())
                            .team(mp.getTeam())
                            .kills(mp.getKills())
                            .deaths(mp.getDeaths())
                            .assists(mp.getAssists())
                            .isWin(mp.isWin())
                            .registeredPlayers(registeredPlayers)
                            .build();
                })
                .collect(Collectors.toList());

        return StatsResponseDto.builder()
                .name(player.getName())
                .tag(player.getTag())
                .tier(player.getTier())
                .cardSmall(player.getCardSmall())
                .totalGames(totalGames)
                .wins(wins)
                .losses(losses)
                .winRate(Math.round(winRate * 10) / 10.0)
                .totalKills(totalKills)
                .totalDeaths(totalDeaths)
                .totalAssists(totalAssists)
                .kd(Math.round(kd * 100) / 100.0)
                .agentStats(agentStats)
                .recentMatches(recentMatches)
                .build();
    }

    public List<MatchHistoryDto> getMatchHistory() {
        return matchRepository.findAll().stream()
                .map(match -> {
                    List<MatchPlayer> participants = matchPlayerRepository.findByMatch(match);
                    if (participants.size() < 3) return null;

                    return MatchHistoryDto.builder()
                            .matchId(match.getMatchId())
                            .map(match.getMap())
                            .winningTeam(match.getWinningTeam())
                            .roundsPlayed(match.getRoundsPlayed())
                            .playedAt(match.getPlayedAt())
                            .registeredPlayers(participants.stream()
                                    .map(mp -> mp.getPlayer().getName())
                                    .collect(Collectors.toList()))
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(MatchHistoryDto::getPlayedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public MatchDetailDto getMatchDetail(String matchId) {
        Match match = matchRepository.findByMatchId(matchId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매치입니다."));

        List<MatchDetailDto.RoundDto> rounds = matchRoundRepository
                .findByMatchOrderByRoundNumberAsc(match)
                .stream()
                .map(r -> MatchDetailDto.RoundDto.builder()
                        .roundNumber(r.getRoundNumber())
                        .winningTeam(r.getWinningTeam())
                        .winCondition(r.getWinCondition())
                        .build())
                .collect(Collectors.toList());

        List<MatchPlayer> registeredPlayers = matchPlayerRepository.findByMatch(match);
        Set<String> registeredPuuids = registeredPlayers.stream()
                .map(mp -> mp.getPlayer().getPuuid())
                .collect(Collectors.toSet());

        List<MatchDetailDto.ParticipantDto> allParticipants = matchParticipantRepository
                .findByMatchOrderByTeamAscKillsDesc(match)
                .stream()
                .map(p -> MatchDetailDto.ParticipantDto.builder()
                        .displayName(p.getDisplayName())
                        .agent(p.getAgent())
                        .team(p.getTeam())
                        .kills(p.getKills())
                        .deaths(p.getDeaths())
                        .assists(p.getAssists())
                        .score(p.getScore())
                        .isWin(p.isWin())
                        .isRegistered(registeredPuuids.contains(p.getPuuid()))
                        .build())
                .collect(Collectors.toList());

        List<MatchDetailDto.ParticipantDto> redTeam = allParticipants.stream()
                .filter(p -> "Red".equalsIgnoreCase(p.getTeam()))
                .collect(Collectors.toList());

        List<MatchDetailDto.ParticipantDto> blueTeam = allParticipants.stream()
                .filter(p -> "Blue".equalsIgnoreCase(p.getTeam()))
                .collect(Collectors.toList());

        String mapImageUrl = "https://media.valorant-api.com/maps/"
                + getMapUuid(match.getMap()) + "/splash.png";

        return MatchDetailDto.builder()
                .matchId(match.getMatchId())
                .map(match.getMap())
                .mapImageUrl(mapImageUrl)
                .winningTeam(match.getWinningTeam())
                .roundsPlayed(match.getRoundsPlayed())
                .playedAt(match.getPlayedAt())
                .rounds(rounds)
                .redTeam(redTeam)
                .blueTeam(blueTeam)
                .build();
    }

    private String getMapUuid(String mapName) {
        return switch (mapName) {
            case "Ascent" -> "7eaecc1b-4337-bbf6-6ab9-04b8f06b3319";
            case "Bind" -> "2c9d57ec-4431-9c5e-4a6c-8d7a0660e2f4";
            case "Haven" -> "2fb9a4fd-47b8-4e7d-a969-74b4046ebd53";
            case "Split" -> "d960549e-485c-e861-8d71-aa9d1aed12a2";
            case "Icebox" -> "e2ad5c54-4114-a870-9641-8ea21279579a";
            case "Breeze" -> "2c09d728-42d5-30d8-43dc-96a05cc7ee9d";
            case "Fracture" -> "b529448b-4d60-346e-e89e-00a4c527a405";
            case "Pearl" -> "fd267378-4d1d-484f-ff52-77821f8ade8f";
            case "Lotus" -> "2fe4ed3a-450a-01a7-d035-1e6d7c64f5b2";
            case "Sunset" -> "92584fbe-486a-b1b2-9faa-39601f7f7ebb";
            case "Abyss" -> "224b0a95-48b9-f703-1bd8-67aca101a61f";
            default -> "";
        };
    }
}