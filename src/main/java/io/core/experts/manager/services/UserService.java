package io.core.experts.manager.services;

import java.util.Set;

import io.core.experts.manager.User;

public interface UserService {
	public User findUserByEmail(String email);
	public User findById(int userId);
	public void saveUser(User user);
	public void updateUser(User user);
	public void updateUser(User user, boolean passwordChanged);
	public String findLoggedInUsername();
	public boolean checkIfCurrentUserIsAuthorizedToPortfolio(long portfolioId);
	public boolean checkIfCurrentUserIsAuthorizedToWallet(long walletId);
//	public boolean checkIfCurrentUserIsAuthorizedToDeposit(long depositId);
//	public boolean checkIfCurrentUserIsAuthorizedToWithdrawal(long withdrawalId);
	public boolean checkIfCurrentUserIsAuthorizedToTransaction(long transactionId);
	public long countByEmail(String email);
	public Set<User> findByPortfolios_Id(Long portfolioId);
}