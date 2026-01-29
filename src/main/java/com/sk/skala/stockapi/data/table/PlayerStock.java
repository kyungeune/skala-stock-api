package com.sk.skala.stockapi.data.table;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "player_stock")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 플레이어 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    // 주식 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // 보유 수량
    private Integer quantity;

    // ✅ Player + Stock + 보유 수량 생성자
    public PlayerStock(Player player, Stock stock, Integer quantity) {
        this.player = player;
        this.stock = stock;
        this.quantity = quantity;
    }
}
