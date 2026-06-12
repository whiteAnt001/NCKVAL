package org.nck.service;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.nck.config.HenrikApiClient;
import org.nck.dto.PlayerResponseDto;
import org.nck.entity.Player;
import org.nck.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final HenrikApiClient henrikApiClient;

    // 플레이어 등록
    public PlayerResponseDto register(String name, String tag) {

        // 이미 등록된 플레이어 체크
        playerRepository.findByNameAndTag(name, tag).ifPresent(p -> {
            throw new IllegalArgumentException("이미 등록된 플레이어입니다.");
        });

        // Henrik API로 계정 정보 조회
        JsonNode account = henrikApiClient.getAccount(name, tag);
        JsonNode mmr = henrikApiClient.getMmr(account.path("region").asText(), name, tag);

        String puuid = account.path("puuid").asText();
        String region = account.path("region").asText();
        int accountLevel = account.path("account_level").asInt();
        String cardSmall = account.path("card").path("small").asText();

        // 티어 정보
        String tier = mmr.path("current_data").path("currenttierpatched").asText("Unranked");
        int tierRank = mmr.path("current_data").path("currenttier").asInt(0);

        Player player = Player.builder()
                .name(name)
                .tag(tag)
                .puuid(puuid)
                .region(region)
                .accountLevel(accountLevel)
                .cardSmall(cardSmall)
                .tier(tier)
                .tierRank(tierRank)
                .build();

        return PlayerResponseDto.from(playerRepository.save(player));
    }

    // 전체 플레이어 조회
    public List<PlayerResponseDto> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(PlayerResponseDto::from)
                .collect(Collectors.toList());
    }

    // 단일 플레이어 조회
    public PlayerResponseDto getPlayer(String name, String tag) {
        Player player = playerRepository.findByNameAndTag(name, tag)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 플레이어입니다."));
        return PlayerResponseDto.from(player);
    }
}