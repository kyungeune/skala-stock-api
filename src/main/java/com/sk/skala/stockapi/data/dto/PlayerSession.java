package com.sk.skala.stockapi.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerSession {

    @NotBlank(message = "playerId는 필수입니다.")
    private String playerId;

    @NotBlank(message = "playerPassword는 필수입니다.")
    private String playerPassword;
}
