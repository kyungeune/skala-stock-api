package com.sk.skala.stockapi.data.table;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주식(Stock) 정보를 저장하기 위한 JPA 엔티티 클래스
 * DB의 stock 테이블과 매핑된다.
 */

@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stockName;  // 주식 이름
    private Double stockPrice;  // 주식 가격

    public Stock(String stockName, Double stockPrice) {  // 주식 생성자 -> ID는 DB에서 자동 생성
        this.stockName = stockName;
        this.stockPrice = stockPrice;
    }
}