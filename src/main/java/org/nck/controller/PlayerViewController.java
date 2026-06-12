package org.nck.controller;

import lombok.RequiredArgsConstructor;
import org.nck.service.BalanceService;
import org.nck.service.PlayerService;
import org.nck.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class PlayerViewController {

    private final PlayerService playerService;
    private final StatsService statsService;
    private final BalanceService balanceService;

    // 메인 (플레이어 목록)
    @GetMapping
    public String index(Model model) {
        model.addAttribute("players", playerService.getAllPlayers());
        return "index";
    }

    // 개인 스탯 페이지
    @GetMapping("/stats/{name}/{tag}")
    public String stats(@PathVariable String name,
                        @PathVariable String tag,
                        Model model) {
        model.addAttribute("stats", statsService.getStats(name, tag));
        return "stats";
    }

    // 팀 밸런싱 페이지
    @GetMapping("/balance")
    public String balancePage(Model model) {
        model.addAttribute("players", playerService.getAllPlayers());
        return "balance";
    }
}
