package io.core.experts.manager.repositories;


import org.springframework.stereotype.Repository;

import io.core.experts.manager.Coin;

import java.util.List;

import org.springframework.data.repository.CrudRepository;


@Repository
public interface CoinRepository extends CrudRepository<Coin, Long> {
	Coin findById(Long id);
	
	List<Coin> findAllByOrderByCoinMarketCapCoinSymbol();
	
	Coin findByCoinMarketCapCoinSymbol(String coinMarketCapSymbol);
	
}

