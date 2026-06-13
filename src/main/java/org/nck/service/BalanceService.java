package org.nck.service;

import lombok.RequiredArgsConstructor;
import org.nck.dto.BalanceResponseDto;
import org.nck.dto.PlayerScoreDto;
import org.nck.entity.MatchPlayer;
import org.nck.entity.Player;
import org.nck.repository.MatchPlayerRepository;
import org.nck.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceService {
    private final PlayerRepository playerRepository;
    private final MatchPlayerRepository matchPlayerRepository;

    public BalanceResponseDto balance(List<Long> playerIds) {

        if(playerIds.size() != 10) {
            throw new IllegalArgumentException("팀 벨런싱은 10명이 필요합니다.");
        }

        List<PlayerScoreDto> scoredPlayers = playerIds.stream()
                .map(id -> playerRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플레이어입니다.")))
                .map(this::calculateSocre)
                .collect(Collectors.toList());

        return findBestBalance(scoredPlayers);
    }

    // 플레이어 점수 계산 (티어점수 70% 내전 승률 30% 반영)
    private PlayerScoreDto calculateSocre(Player player) {
        List<MatchPlayer> records = matchPlayerRepository.findByPlayer(player);

        double winRate = 0.0;
        if(!records.isEmpty()) {
            long wins = records.stream().filter(MatchPlayer::isWin).count();
            winRate = (double) wins / records.size() * 100;
        }

        // 티어점수
        double tierScore = player.getTierRank() * 0.8;
        double winRateScore = winRate * 0.2;
        double totalScore = tierScore + winRateScore;

        return PlayerScoreDto.builder()
                .id(player.getId())
                .name(player.getName())
                .tag(player.getTag())
                .tier(player.getTier())
                .cardSmall(player.getCardSmall())
                .winRate(Math.round(winRate * 10) / 10)
                .score(Math.round(totalScore * 10) / 10)
                .build();
    }

    // 점수차가 최소인 팀 찾기
    private BalanceResponseDto findBestBalance(List<PlayerScoreDto> players) {
        double bestDiff = Double.MAX_VALUE;
        List<PlayerScoreDto> bestTeamA = null;
        List<PlayerScoreDto> bestTeamB = null;

        List<List<Integer>> combinations = getCombinations(10 ,5);

        for(List<Integer> combo : combinations) {
            List<PlayerScoreDto> teamA = combo.stream()
                    .map(players::get)
                    .collect(Collectors.toList());

            List<PlayerScoreDto> teamB = new ArrayList<>();
            for(int i = 0; i < 10; i++) {
                if (!combo.contains(i)) {
                    teamB.add(players.get(i));
                }
            }

            double scoreA = teamA.stream().mapToDouble(PlayerScoreDto::getScore).sum();
            double scoreB = teamB.stream().mapToDouble(PlayerScoreDto::getScore).sum();
            double diff = Math.abs(scoreA - scoreB);

            if(diff < bestDiff) {
                bestDiff = diff;
                bestTeamA = teamA;
                bestTeamB = teamB;
            }
        }

        double scoreA = bestTeamA.stream().mapToDouble(PlayerScoreDto::getScore).sum();
        double scoreB = bestTeamB.stream().mapToDouble(PlayerScoreDto::getScore).sum();

        return BalanceResponseDto.builder()
                .teamA(bestTeamA)
                .teamB(bestTeamB)
                .teamAScore(Math.round(scoreA * 10) / 10.0)
                .teamBScore(Math.round(scoreB * 10) / 10.0)
                .scoreDiff(Math.round(bestDiff * 10) / 10.0)
                .build();
    }

    // 조합생성
    private List<List<Integer>> getCombinations(int n, int r) {
        List<List<Integer>> result = new ArrayList<>();
        combine(n, r, 0, new ArrayList<>(), result);
        return result;
    }

    private void combine(int n, int r, int start, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == r) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < n; i++) {
            current.add(i);
            combine(n, r, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
}
