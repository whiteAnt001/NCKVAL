package org.nck.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MatchHistoryDto {
    private String matchId;
    private String map;
    private String winningTeam;
    private int roundsPlayed;
    private LocalDateTime playedAt;

    // 해당 플레이어 스탯
    private String agent;
    private String team;
    private int kills;
    private int deaths;
    private int assists;
    private boolean isWin;

    // 참여한 등록 플레이어 목록
    private List<String> registeredPlayers;
}