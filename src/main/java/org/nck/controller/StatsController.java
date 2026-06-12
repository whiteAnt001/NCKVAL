package org.nck.controller;

import lombok.RequiredArgsConstructor;
import org.nck.dto.StatsResponseDto;
import org.nck.entity.Match;
import org.nck.service.MatchService;
import org.nck.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;
    private final MatchService matchService;

    // 매치 동기화
    @PostMapping("/sync/{name}/{tag}")
    public ResponseEntity<String> sync(@PathVariable String name,
                                       @PathVariable String tag) {
        int saved = matchService.syncMatches(name, tag);
        return ResponseEntity.ok(saved + "개 매치 동기화 완료");
    }

    // rodls tmxot whghl
    @GetMapping("/{name}/{tag}")
    public ResponseEntity<StatsResponseDto> getStats(@PathVariable String name,
                                                     @PathVariable String tag) {
        return ResponseEntity.ok(statsService.getStats(name, tag));
    }

    // 전체 플레이어 동기화
    @PostMapping("/sync/all")
    public ResponseEntity<String> syncAll() {
        int saved = matchService.syncAllMatches();
        return ResponseEntity.ok(saved + "개 매치 동기화 완료");
    }
}
