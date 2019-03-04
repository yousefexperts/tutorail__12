package io.core.experts.manager.repositories;

import org.springframework.data.repository.CrudRepository;

import io.core.experts.manager.TransactionType;

public interface TransactionTypeRepository extends CrudRepository<TransactionType, Long> {
	TransactionType findByType(String type);
}
