package org.nck.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MatchDetailDto {
    private String matchId;
    private String map;
    private String mapImageUrl;
    private String winningTeam;
    private int roundsPlayed;
    private LocalDateTime playedAt;

    private List<RoundDto> rounds;
    private List<ParticipantDto> redTeam;
    private List<ParticipantDto> blueTeam;

    @Getter
    @Builder
    public static class RoundDto {
        private int roundNumber;
        private String winningTeam;
        private String winCondition;
    }

    @Getter
    @Builder
    public static class ParticipantDto {
        private String displayName;
        private String agent;
        private String team;
        private int kills;
        private int deaths;
        private int assists;
        private int score;
        private boolean isWin;
        private boolean isRegistered; // 등록된 플레이어 여부
    }
}