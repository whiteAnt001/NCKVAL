package org.nck.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BalanceResponseDto {
    private List<PlayerScoreDto> teamA;
    private List<PlayerScoreDto> teamB;
    private double teamAScore;
    private double teamBScore;
    private double scoreDiff;
}
