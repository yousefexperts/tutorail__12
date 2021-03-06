package io.core.experts.manager.repositories;


import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import io.core.experts.manager.Portfolio;


//This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
//CRUD refers Create, Read, Update, Delete

public interface PortfolioRepository extends CrudRepository<Portfolio, Long> {
	Portfolio findById(Long id);	
	
	Portfolio findByName(String name);
	
	Set<Portfolio> findByUsers_email(String email);
	
	Set<Portfolio> findByApiKeys_apiKey(String apiKey);
	
}
