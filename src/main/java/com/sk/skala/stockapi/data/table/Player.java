package com.sk.skala.stockapi.data.table;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 플레이어(Player) 정보를 저장하기 위한 JPA 엔티티 클래스
 * DB의 player 테이블과 매핑된다.
 */

@Entity
@Table(name = "player")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player {

    @Id
    private String playerId;  // 플레이어 ID

    private String playerPassword;  // 플레이어 비밀번호

    private Double playerMoney;  // 플레이어가 보유하고 있는 자금

    public Player(String playerId, Double playerMoney) {  // 플레이어 생성자 -> ID
        this.playerId = playerId;
        this.playerMoney = playerMoney;
    }
}