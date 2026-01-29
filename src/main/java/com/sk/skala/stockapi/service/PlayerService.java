package com.sk.skala.stockapi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.dto.PlayerStockDto;
import com.sk.skala.stockapi.data.dto.PlayerStockListDto;
import com.sk.skala.stockapi.data.dto.Response;
import com.sk.skala.stockapi.data.table.Player;
import com.sk.skala.stockapi.data.table.PlayerStock;
import com.sk.skala.stockapi.data.table.Stock;
import com.sk.skala.stockapi.data.dto.StockOrder;
import com.sk.skala.stockapi.data.dto.PlayerSession;
import com.sk.skala.stockapi.exception.ParameterException;
import com.sk.skala.stockapi.exception.ResponseException;
import com.sk.skala.stockapi.repository.PlayerRepository;
import com.sk.skala.stockapi.repository.PlayerStockRepository;
import com.sk.skala.stockapi.repository.StockRepository;
import com.sk.skala.stockapi.service.SessionHandler;

import lombok.RequiredArgsConstructor;

/**
 * Player 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 플레이어 생성, 조회, 로그인 및 주식 매수/매도 기능을 담당한다.
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerService {

    private final StockRepository stockRepository;  // 주식 조회용 Repository
    private final PlayerRepository playerRepository;  // 플레이어 정보 관리 Repository
    private final PlayerStockRepository playerStockRepository;  // 플레이어-주식 관계 관리 Repository
    private final SessionHandler sessionHandler;  // 로그인 세션 처리 컴포넌트

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
    @Transactional(readOnly = true)
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
                50000.0   // 초기 자산
        );
        player.setPlayerPassword(playerSession.getPlayerPassword());

        Player saved = playerRepository.save(player);

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
    public Response loginPlayer(PlayerSession playerSession) {
        if (playerSession == null
                || playerSession.getPlayerId() == null
                || playerSession.getPlayerId().isBlank()
                || playerSession.getPlayerPassword() == null
                || playerSession.getPlayerPassword().isBlank()) {
            throw new ParameterException("playerId", "playerPassword");
        }

        Player player = playerRepository.findById(playerSession.getPlayerId())
                .orElseThrow(() ->
                        new ResponseException(Error.DATA_NOT_FOUND));

        if (!player.getPlayerPassword().equals(playerSession.getPlayerPassword())) {
            throw new ResponseException(Error.NOT_AUTHENTICATED);
        }

        String accessToken = sessionHandler.createAccessToken(player.getPlayerId());

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(accessToken)
                .build();
    }

    // =========================
    // 플레이어 정보 수정
    // =========================
    @Transactional
    public Response updatePlayer(Player player) {
        Player found = playerRepository.findById(player.getPlayerId())
                .orElseThrow(() ->
                        new ResponseException(Error.DATA_NOT_FOUND));

        found.setPlayerMoney(player.getPlayerMoney());

        Player saved = playerRepository.save(found);

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
        Player found = playerRepository.findById(player.getPlayerId())
                .orElseThrow(() ->
                        new ResponseException(Error.DATA_NOT_FOUND));

        playerRepository.delete(found);

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(found)
                .build();
    }

    // =========================
    // 주식 매수
    // =========================
    @Transactional
    public Response buyPlayerStock(StockOrder order) {
        Player player = playerRepository.findById(order.getPlayerId())
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

        playerStock.setQuantity(
                playerStock.getQuantity() + order.getStockQuantity());

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
    // 주식 매도
    // =========================
    @Transactional
    public Response sellPlayerStock(StockOrder order) {
        Player player = playerRepository.findById(order.getPlayerId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        Stock stock = stockRepository.findById(order.getStockId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        PlayerStock playerStock =
                playerStockRepository.findByPlayerAndStock(player, stock)
                        .orElseThrow(() ->
                                new ResponseException(Error.INSUFFICIENT_QUANTITY));

        if (playerStock.getQuantity() < order.getStockQuantity()) {
            throw new ResponseException(Error.INSUFFICIENT_QUANTITY);
        }

        playerStock.setQuantity(
                playerStock.getQuantity() - order.getStockQuantity());

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
