package com.sk.skala.stockapi.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sk.skala.stockapi.data.dto.Response;
import com.sk.skala.stockapi.data.table.Stock;
import com.sk.skala.stockapi.service.StockService;

import lombok.RequiredArgsConstructor;

/**
 * Stock(주식) 관련 API 요청을 처리하는 REST 컨트롤러
 * 주식 조회, 등록, 수정, 삭제 기능을 제공한다.
 */

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // 전체 주식 목록 조회 API
    @GetMapping("/list")
    public Response getAllStocks(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer count
    ) {
        return stockService.getAllStocks(offset, count);
    }

    // 개별 주식 상세 조회 API
    @GetMapping("/{id}")
    public Response getStockById(@PathVariable Long id) {
        return stockService.getStockById(id);
    }

    // 주식 등록 API
    @PostMapping
    public Response createStock(@RequestBody Stock stock) {
        return stockService.createStock(stock);
    }

    // 주식 정보 수정 API
    @PutMapping
    public Response updateStock(@RequestBody Stock stock) {
        return stockService.updateStock(stock);
    }

    // 주식 삭제 API
    @DeleteMapping
    public Response deleteStock(@RequestBody Stock stock) {
        return stockService.deleteStock(stock);
    }
}
