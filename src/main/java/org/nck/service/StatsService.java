package org.nck.service;

import lombok.RequiredArgsConstructor;
import org.nck.dto.AgentStatsDto;
import org.nck.dto.StatsResponseDto;
import org.nck.entity.MatchPlayer;
import org.nck.entity.Player;
import org.nck.repository.MatchPlayerRepository;
import org.nck.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final PlayerRepository playerRepository;
    private final MatchPlayerRepository matchPlayerRepository;

    // 플레이어 승률 및 전적 조회
    public StatsResponseDto getStats(String name, String tag) {
        Player player = playerRepository.findByNameAndTag(name, tag)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 플레이어 입니다."));

        List<MatchPlayer> matchPlayers = matchPlayerRepository.findByPlayer(player);

        int totalGames = matchPlayers.size();
        int wins = (int) matchPlayers.stream().filter(MatchPlayer::isWin).count();
        int losses = totalGames - wins;
        double winRate = totalGames == 0 ? 0.0 : (double) wins / totalGames * 100;

        //kda
        int totalKills = matchPlayers.stream().mapToInt(MatchPlayer::getKills).sum();
        int totalDeaths = matchPlayers.stream().mapToInt(MatchPlayer::getDeaths).sum();
        int totalAssists = matchPlayers.stream().mapToInt(MatchPlayer::getAssists).sum();
        double kda = totalDeaths == 0 ? totalKills + totalAssists : (double) (totalKills + totalAssists) / totalDeaths;

        // 요원별 픽률 + 승률
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
                .kda(Math.round(kda * 100) / 100.0)
                .agentStats(agentStats)
                .build();
    }
}
