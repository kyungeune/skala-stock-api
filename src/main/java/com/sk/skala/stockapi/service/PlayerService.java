package com.sk.skala.stockapi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.dto.PlayerSession;
import com.sk.skala.stockapi.data.dto.PlayerStockDto;
import com.sk.skala.stockapi.data.dto.PlayerStockListDto;
import com.sk.skala.stockapi.data.dto.Response;
import com.sk.skala.stockapi.data.dto.StockOrder;
import com.sk.skala.stockapi.data.table.Player;
import com.sk.skala.stockapi.data.table.PlayerStock;
import com.sk.skala.stockapi.data.table.Stock;
import com.sk.skala.stockapi.exception.ParameterException;
import com.sk.skala.stockapi.exception.ResponseException;
import com.sk.skala.stockapi.repository.PlayerRepository;
import com.sk.skala.stockapi.repository.PlayerStockRepository;
import com.sk.skala.stockapi.repository.StockRepository;

import lombok.RequiredArgsConstructor;

/**
 * Player 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 플레이어 생성, 조회, 로그인 및 주식 매수/매도 기능을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerService {

    private final StockRepository stockRepository;
    private final PlayerRepository playerRepository;
    private final PlayerStockRepository playerStockRepository;
    private final SessionHandler sessionHandler;

    // =========================
    // 전체 플레이어 목록 조회
    // =========================
    public Response getAllPlayers(int offset, int count) {
        Pageable pageable = PageRequest.of(offset, count);
        List<Player> players = playerRepository.findAll(pageable).getContent();

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(players)
                .build();
    }

    // =========================
    // 단일 플레이어 보유 주식 목록 조회
    // =========================
    public Response getPlayerById(String playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() ->
                        new ResponseException(Error.DATA_NOT_FOUND, "Player not found"));

        List<PlayerStock> playerStocks =
                playerStockRepository.findByPlayer_PlayerId(playerId);

        List<PlayerStockDto> stockDtos = playerStocks.stream()
                .map(ps -> PlayerStockDto.builder()
                        .stockId(ps.getStock().getId())
                        .stockName(ps.getStock().getStockName())
                        .stockPrice(ps.getStock().getStockPrice())
                        .quantity(ps.getQuantity())
                        .build())
                .collect(Collectors.toList());

        PlayerStockListDto dto = PlayerStockListDto.builder()
                .playerId(player.getPlayerId())
                .playerMoney(player.getPlayerMoney())
                .stocks(stockDtos)
                .build();

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(dto)
                .build();
    }

    // =========================
    // 플레이어 생성
    // =========================
    @Transactional
    public Response createPlayer(PlayerSession playerSession) {
        if (playerSession == null
                || playerSession.getPlayerId() == null
                || playerSession.getPlayerId().isBlank()
                || playerSession.getPlayerPassword() == null
                || playerSession.getPlayerPassword().isBlank()) {
            throw new ParameterException("playerId", "playerPassword");
        }

        if (playerRepository.existsById(playerSession.getPlayerId())) {
            throw new ResponseException(Error.DATA_DUPLICATED);
        }

        Player player = new Player(
                playerSession.getPlayerId(),
                50000.0
        );
        player.setPlayerPassword(playerSession.getPlayerPassword());

        Player saved = playerRepository.save(player);

        // (선택) 응답에 password 노출 방지 - 과제/요구사항에 안전
        saved.setPlayerPassword("");

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(saved)
                .build();
    }

    // =========================
    // 플레이어 로그인
    // =========================
    @Transactional
    public Response loginPlayer(PlayerSession playerSession) {
        if (playerSession == null
                || playerSession.getPlayerId() == null
                || playerSession.getPlayerId().isBlank()
                || playerSession.getPlayerPassword() == null
                || playerSession.getPlayerPassword().isBlank()) {
            throw new ParameterException("playerId", "playerPassword");
        }

        Player player = playerRepository.findById(playerSession.getPlayerId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Player not found"));

        if (!player.getPlayerPassword().equals(playerSession.getPlayerPassword())) {
            throw new ResponseException(Error.NOT_AUTHENTICATED);
        }

        // ✅ 사진 요구사항: 인증 성공 시 세션/쿠키에 토큰 저장
        // SessionHandler는 password를 비워서 토큰에 넣고, 쿠키 저장까지 수행
        sessionHandler.storeAccessToken(PlayerSession.builder()
                .playerId(player.getPlayerId())
                .playerPassword("") // 토큰에 절대 넣지 않음
                .build());

        // ✅ 사진 요구사항: player 정보를 body에 담아 반환 + password 숨김 처리
        player.setPlayerPassword("");

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(player)
                .build();
    }

    // =========================
    // 플레이어 정보 수정
    // =========================
    @Transactional
    public Response updatePlayer(Player player) {
        // 사진에 "유효성 체크"가 있으므로 최소한 방어 코드 추가
        if (player == null || player.getPlayerId() == null || player.getPlayerId().isBlank()) {
            throw new ResponseException(Error.DATA_NOT_FOUND);
        }

        Player found = playerRepository.findById(player.getPlayerId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        found.setPlayerMoney(player.getPlayerMoney());

        Player saved = playerRepository.save(found);
        saved.setPlayerPassword("");

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(saved)
                .build();
    }

    // =========================
    // 플레이어 삭제
    // =========================
    @Transactional
    public Response deletePlayer(Player player) {
        if (player == null || player.getPlayerId() == null || player.getPlayerId().isBlank()) {
            throw new ResponseException(Error.DATA_NOT_FOUND);
        }

        Player found = playerRepository.findById(player.getPlayerId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        playerRepository.delete(found);
        found.setPlayerPassword("");

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(found)
                .build();
    }

    // =========================
    // 주식 매수 (세션 기반 playerId)
    // =========================
    @Transactional
    public Response buyPlayerStock(StockOrder order) {
        // ✅ 사진 요구사항: 현재 로그인 플레이어 가져오기 (SessionHandler)
        String playerId = sessionHandler.getPlayerId();
        if (playerId == null || playerId.isBlank()) {
            throw new ResponseException(Error.SESSION_NOT_FOUND);
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        Stock stock = stockRepository.findById(order.getStockId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        double totalPrice = stock.getStockPrice() * order.getStockQuantity();
        if (player.getPlayerMoney() < totalPrice) {
            throw new ResponseException(Error.INSUFFICIENT_FUNDS);
        }

        PlayerStock playerStock =
                playerStockRepository.findByPlayerAndStock(player, stock)
                        .orElseGet(() -> new PlayerStock(player, stock, 0));

        playerStock.setQuantity(playerStock.getQuantity() + order.getStockQuantity());
        player.setPlayerMoney(player.getPlayerMoney() - totalPrice);

        playerStockRepository.save(playerStock);
        playerRepository.save(player);

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .build();
    }

    // =========================
    // 주식 매도 (세션 기반 playerId)
    // =========================
    @Transactional
    public Response sellPlayerStock(StockOrder order) {
        // ✅ 사진 요구사항: 현재 로그인 플레이어 가져오기 (SessionHandler)
        String playerId = sessionHandler.getPlayerId();
        if (playerId == null || playerId.isBlank()) {
            throw new ResponseException(Error.SESSION_NOT_FOUND);
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        Stock stock = stockRepository.findById(order.getStockId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        PlayerStock playerStock =
                playerStockRepository.findByPlayerAndStock(player, stock)
                        .orElseThrow(() -> new ResponseException(Error.INSUFFICIENT_QUANTITY));

        if (playerStock.getQuantity() < order.getStockQuantity()) {
            throw new ResponseException(Error.INSUFFICIENT_QUANTITY);
        }

        playerStock.setQuantity(playerStock.getQuantity() - order.getStockQuantity());

        double totalPrice = stock.getStockPrice() * order.getStockQuantity();
        player.setPlayerMoney(player.getPlayerMoney() + totalPrice);

        if (playerStock.getQuantity() == 0) {
            playerStockRepository.delete(playerStock);
        } else {
            playerStockRepository.save(playerStock);
        }

        playerRepository.save(player);

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .build();
    }
}
