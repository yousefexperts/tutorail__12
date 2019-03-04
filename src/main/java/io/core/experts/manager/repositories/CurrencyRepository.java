package io.core.experts.manager.repositories;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.core.experts.manager.Currency;


@Repository
public interface CurrencyRepository extends CrudRepository<Currency, Long> {
	Currency findByCurrencyISOCode(String currencyISOCode);
}
