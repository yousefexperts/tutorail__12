package io.core.experts.manager.repositories;


import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.core.experts.manager.User;


@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Integer> {
	 User findByEmail(String email);
	 User findById(int userId);
	 
	 long countByEmail(String email);
	 
	 Set<User> findByPortfolios_Id(Long portfolioId);
	 
	 
}