package io.core.experts.manager.repositories;

import org.springframework.data.repository.CrudRepository;

import io.core.experts.manager.Portfolio;
import io.core.experts.manager.Settings;

//This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
//CRUD refers Create, Read, Update, Delete

public interface SettingsRepository extends CrudRepository<Settings, Long> {
		
	
}
