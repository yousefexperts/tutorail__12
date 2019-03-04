package io.core.experts.manager.repositories;

import org.springframework.data.repository.CrudRepository;

import io.core.experts.manager.PasswordResetToken;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, Long>{
	PasswordResetToken findByToken(String token);
}
