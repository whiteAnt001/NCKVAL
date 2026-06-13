package org.nck.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StatsResponseDto {
    private String name;
    private String tag;
    private String tier;
    private String cardSmall;

    private int totalGames;
    private int wins;
    private int losses;
    private double winRate;

    private int totalKills;
    private int totalDeaths;
    private int totalAssists;
    private double kda;

    private List<AgentStatsDto> agentStats;

    private List<MatchHistoryDto> recentMatches;

}
