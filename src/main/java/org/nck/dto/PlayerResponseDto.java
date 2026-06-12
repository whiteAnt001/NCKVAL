package org.nck.dto;

import lombok.Builder;
import lombok.Getter;
import org.nck.entity.Player;

@Getter
@Builder
public class PlayerResponseDto {

    private Long id;
    private String name;
    private String tag;
    private String puuid;
    private String region;
    private int accountLevel;
    private String tier;
    private int tierRank;
    private String cardSmall;

    public static PlayerResponseDto from(Player player) {
        return PlayerResponseDto.builder()
                .id(player.getId())
                .name(player.getName())
                .tag(player.getTag())
                .puuid(player.getPuuid())
                .region(player.getRegion())
                .accountLevel(player.getAccountLevel())
                .tier(player.getTier())
                .tierRank(player.getTierRank())
                .cardSmall(player.getCardSmall())
                .build();
    }
}
