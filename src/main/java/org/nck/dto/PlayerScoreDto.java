package org.nck.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlayerScoreDto {
    private Long id;
    private String name;
    private String tag;
    private String tier;
    private String cardSmall;
    private double winRate;
    private double score;
}
