package com.sk.skala.stockapi.data.table;

import jakarta.persistence.Id;

public class Stock {

	@Id
	private Long id;

	public Stock(String name, Double price) {
		// implement codes here
	}
}
