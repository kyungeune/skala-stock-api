package com.sk.skala.stockapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sk.skala.stockapi.data.table.PlayerStock;

public interface PlayerStockRepository extends JpaRepository<PlayerStock, Long> {

}