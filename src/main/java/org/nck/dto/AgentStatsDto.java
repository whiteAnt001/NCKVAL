package org.nck.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AgentStatsDto {
    private String agent;
    private int picks;
    private int wins;
    private double winRate;
}
