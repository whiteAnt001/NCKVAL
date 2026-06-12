package org.nck.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.nck.dto.BalanceRequestDto;
import org.nck.dto.BalanceResponseDto;
import org.nck.service.BalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/balance")
public class BalanceController {
    private final BalanceService balanceService;

    // 팀 밸런싱
    @PostMapping
    public ResponseEntity<BalanceResponseDto> balance(@RequestBody BalanceRequestDto dto) {
        return ResponseEntity.ok(balanceService.balance(dto.getPlayerIds()));
    }
}
