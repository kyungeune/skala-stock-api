package com.sk.skala.stockapi.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.dto.Response;
import com.sk.skala.stockapi.data.table.Stock;
import com.sk.skala.stockapi.exception.ParameterException;
import com.sk.skala.stockapi.exception.ResponseException;
import com.sk.skala.stockapi.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;

    // 전체 주식 목록 조회 (페이지)
    public Response getAllStocks(int offset, int count) {
        Pageable pageable = PageRequest.of(offset, count);
        List<Stock> stocks = stockRepository.findAll(pageable).getContent();

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(stocks)
                .build();
    }

    // 개별 주식 상세 조회
    public Response getStockById(Long id) {
        if (id == null) {
            throw new ParameterException("id");
        }

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(stock)
                .build();
    }

    // 주식 등록
    @Transactional
    public Response createStock(Stock stock) {
        validateStock(stock);

        stockRepository.findByStockName(stock.getStockName())
                .ifPresent(s -> { throw new ResponseException(Error.DATA_DUPLICATED); });

        Stock saved = stockRepository.save(stock);

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(saved)
                .build();
    }

    // 주식 수정
    @Transactional
    public Response updateStock(Stock stock) {
        validateStock(stock);

        if (stock.getId() == null) {
            throw new ParameterException("id");
        }

        Stock found = stockRepository.findById(stock.getId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        // ✅ Stock 엔티티에 setter가 없으면 여기서 컴파일 오류가 납니다.
        found.setStockName(stock.getStockName());
        found.setStockPrice(stock.getStockPrice());

        Stock saved = stockRepository.save(found);

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(saved)
                .build();
    }

    // 주식 삭제
    @Transactional
    public Response deleteStock(Stock stock) {
        if (stock == null || stock.getId() == null) {
            throw new ParameterException("id");
        }

        Stock found = stockRepository.findById(stock.getId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        stockRepository.delete(found);

        return Response.builder()
                .result(1)
                .code(0)
                .message("OK")
                .body(found)
                .build();
    }

    private void validateStock(Stock stock) {
        if (stock == null) {
            throw new ParameterException("stockName", "stockPrice");
        }
        if (stock.getStockName() == null || stock.getStockName().isBlank()
                || stock.getStockPrice() == null || stock.getStockPrice() <= 0) {
            throw new ParameterException("stockName", "stockPrice");
        }
    }
}
