package com.sk.skala.stockapi.data.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockOrder {

    @NotBlank(message = "playerId는 필수입니다.")
    private String playerId;

    @NotNull(message = "stockId는 필수입니다.")
    private Long stockId;

    @NotNull(message = "stockQuantity는 필수입니다.")
    @Min(value = 1, message = "stockQuantity는 1 이상이어야 합니다.")
    private Integer stockQuantity;
}
