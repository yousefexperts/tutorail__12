package io.core.experts.manager.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import io.core.experts.manager.ApiKey;
import io.core.experts.manager.User;


public interface ApiKeyRepository extends CrudRepository<ApiKey, Long> {
	ApiKey findApiKeyByApiKey(String apiKey);
	
	ApiKey findById(Long apiKeyId);

	List<ApiKey> findByUser(User user);
	
	
	
}
