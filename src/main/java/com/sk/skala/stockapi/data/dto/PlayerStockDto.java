package com.sk.skala.stockapi.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStockDto {

    private Long stockId;
    private String stockName;
    private Double stockPrice;
    private Integer quantity;
}
