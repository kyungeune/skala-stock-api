package com.sk.skala.stockapi.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockOrder {

    private String playerId;
    private Long stockId;
    private Integer stockQuantity;
}
