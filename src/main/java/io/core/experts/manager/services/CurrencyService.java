package io.core.experts.manager.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.core.experts.manager.Currency;
import io.core.experts.manager.repositories.CurrencyRepository;

@Service
public class CurrencyService {
	@Autowired
	private CurrencyRepository currencyRepository;
	
	public Currency findByCurrencyISOCode(String currencyISOCode) {
		return currencyRepository.findByCurrencyISOCode(currencyISOCode);
	}
	
	public void save(Currency currency) {
		currencyRepository.save(currency);
	}
}
