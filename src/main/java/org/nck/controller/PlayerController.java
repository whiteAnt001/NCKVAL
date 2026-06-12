package org.nck.controller;

import lombok.RequiredArgsConstructor;
import org.nck.dto.PlayerRegisterRequestDto;
import org.nck.dto.PlayerResponseDto;
import org.nck.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    // 플레이어 등록
    @PostMapping
    public ResponseEntity<PlayerResponseDto> register(@RequestBody PlayerRegisterRequestDto request) {
        return ResponseEntity.ok(playerService.register(request.getName(), request.getTag()));
    }

    // 전체 플레이어 조회
    @GetMapping
    public ResponseEntity<List<PlayerResponseDto>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    // 단일 플레이어 조회
    @GetMapping("/{name}/{tag}")
    public ResponseEntity<PlayerResponseDto> getPlayer(
            @PathVariable String name,
            @PathVariable String tag) {
        return ResponseEntity.ok(playerService.getPlayer(name, tag));
    }
}
