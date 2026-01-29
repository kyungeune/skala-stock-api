package com.sk.skala.stockapi.controller;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.sk.skala.stockapi.data.dto.PlayerSession;
import com.sk.skala.stockapi.data.dto.Response;
import com.sk.skala.stockapi.data.dto.StockOrder;
import com.sk.skala.stockapi.data.table.Player;
import com.sk.skala.stockapi.service.PlayerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/players")
@Validated
public class PlayerController {

    private final PlayerService playerService;

    // 전체 플레이어 목록 조회
    @GetMapping("/list")
    public Response getAllPlayers(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "count", defaultValue = "10") int count
    ) {
        return playerService.getAllPlayers(offset, count);
    }

    // 단일 플레이어 상세 조회 + 보유 주식 목록
    @GetMapping("/{playerId}")
    public Response getPlayerById(@PathVariable String playerId) {
        return playerService.getPlayerById(playerId);
    }

    // 플레이어 생성
    @PostMapping
    public Response createPlayer(@Valid @RequestBody PlayerSession playerSession) {
        return playerService.createPlayer(playerSession);
    }

    // 플레이어 로그인
    @PostMapping("/login")
    public Response loginPlayer(@Valid @RequestBody PlayerSession playerSession) {
        return playerService.loginPlayer(playerSession);
    }

    // 플레이어 정보 수정
    @PutMapping
    public Response updatePlayer(@RequestBody Player player) {
        return playerService.updatePlayer(player);
    }

    // 플레이어 삭제
    @DeleteMapping
    public Response deletePlayer(@RequestBody Player player) {
        return playerService.deletePlayer(player);
    }

    // 주식 매수
    @PostMapping("/buy")
    public Response buyPlayerStock(@Valid @RequestBody StockOrder order) {
        return playerService.buyPlayerStock(order);
    }

    // 주식 매도
    @PostMapping("/sell")
    public Response sellPlayerStock(@Valid @RequestBody StockOrder order) {
        return playerService.sellPlayerStock(order);
    }
}
