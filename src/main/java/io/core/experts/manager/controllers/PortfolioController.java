package io.core.experts.manager.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.core.experts.manager.ApiKey;
import io.core.experts.manager.ApiRequestHandler;
import io.core.experts.manager.Coin;
import io.core.experts.manager.CoinMarketCapCoin;
import io.core.experts.manager.CoinValue;
import io.core.experts.manager.Currency;
import io.core.experts.manager.FormOption;
import io.core.experts.manager.FormOptions;
import io.core.experts.manager.Portfolio;
import io.core.experts.manager.PortfolioChartLine;
import io.core.experts.manager.PortfolioChartLineWallet;
import io.core.experts.manager.PortfolioLineChartRoiValue;
import io.core.experts.manager.PortfolioPieChartValue;
import io.core.experts.manager.User;
import io.core.experts.manager.Wallet;
import io.core.experts.manager.repositories.CoinValueRepository;
import io.core.experts.manager.repositories.PortfolioRepository;
import io.core.experts.manager.repositories.UserRepository;
import io.core.experts.manager.repositories.WalletRepository;
import io.core.experts.manager.services.ApiKeyService;
import io.core.experts.manager.services.PortfolioService;
import io.core.experts.manager.services.TransactionService;
import io.core.experts.manager.services.UserService;
import io.core.experts.manager.services.WalletService;

@Controller    // This means that this class is a Controller
@RequestMapping(path="/portfolio") // This means URL's start with /portfolio (after Application path)
public class PortfolioController {
	@Autowired
	private PortfolioService portfolioService;
	
	@Autowired
	private WalletService walletService;
	
	@Autowired
	@Qualifier(value = "coinValueRepository")
	private CoinValueRepository coinValueRepository;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ApiKeyService apiKeyService;
	
	@Resource(name = "currency")
	private Currency currency;

	// send the form
	@GetMapping("/add")
    public String portfolioForm(Model model) {
		model.addAttribute("portfolio", new Portfolio());
        return "portfolio_form";
    }
	
	// handle the form
	@PostMapping(path="/add") // Map ONLY Post Requests
	public String addNewPortfolio (
			@RequestParam String description,
			@RequestParam String name, 
			Model model) {
				
		portfolioService.createNewPortfolio(name, description);
				
		return "redirect:/portfolio/results";
		
	}
	
	@GetMapping("/edit/{portfolioId}")
    public String editPortfolio(@PathVariable("portfolioId") long portfolioId, Model model) {
		Portfolio portfolio = portfolioService.findById(portfolioId);
		
		// check if the object exists
		if(!portfolioService.checkIfPortfolioExists(portfolioId)) {
			model.addAttribute("notFoundError", true);
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			User user = userService.findUserByEmail(userService.findLoggedInUsername());
			model.addAttribute("authorizationError", true);
			model.addAttribute("firstName", user.getName());
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		model.addAttribute("portfolio", portfolio);
        return "portfolio_edit";
    }
	
	@PostMapping(path="/edit/{portfolioId}")
	public String updatePortfolio (
			@PathVariable("portfolioId") long portfolioId,
			@RequestParam String description,
			@RequestParam String name, 
			Model model) {
		
		// check if the object exists
		if(!portfolioService.checkIfPortfolioExists(portfolioId)) {
			model.addAttribute("notFoundError", true);
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			User user = userService.findUserByEmail(userService.findLoggedInUsername());
			model.addAttribute("authorizationError", true);
			model.addAttribute("firstName", user.getName());
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		portfolioService.updatePortfolio(portfolioId, name, description);
				
		return "redirect:/portfolio/details/" + portfolioId;
		
	}
	
	@GetMapping("/results")
    public String portfolioResults(Model model) {

		// get the portfolios for the logged in user
		Set<Portfolio> portfolioList = portfolioService.findByUsers_email(userService.findLoggedInUsername());
		
		model.addAttribute("currency", currency);
		model.addAttribute("portfolioList", portfolioList);
		
		
        return "portfolio_results";
        
    }
	
	// get portfolio details
	@RequestMapping(value = "/details/{portfolioId}", method = RequestMethod.GET)
	public String getWalletsByPortfolioId(@PathVariable("portfolioId") long portfolioId, Model model) {
		// check if the object exists
		if(!portfolioService.checkIfPortfolioExists(portfolioId)) {
			model.addAttribute("notFoundError", true);
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			User user = userService.findUserByEmail(userService.findLoggedInUsername());
			model.addAttribute("authorizationError", true);
			model.addAttribute("firstName", user.getName());
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		
		// get the details
		Portfolio portfolio = portfolioService.findById(portfolioId);
		// add to model		
		model.addAttribute("portfolio", portfolio);
		model.addAttribute("currency", currency);
		
		// now get the attached wallets
		List<Wallet> wallets = walletService.getWalletsByPortfolio(portfolio);
		
		// get the amount of wallets
		int countWallets = wallets.size();
		// add to the model
		model.addAttribute("countWallets", countWallets);
		
		// add the wallets to the model
		model.addAttribute("wallets", wallets);
		
		
		return "portfolio_details";
	}
	
	// handle get for the access page
	@RequestMapping(value = "/access/{portfolioId}", method = RequestMethod.GET)
	public String grantAccessToPortfolio(@PathVariable("portfolioId") long portfolioId, Model model) {
		// check if the object exists
		if(!portfolioService.checkIfPortfolioExists(portfolioId)) {
			model.addAttribute("notFoundError", true);
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			User user = userService.findUserByEmail(userService.findLoggedInUsername());
			model.addAttribute("authorizationError", true);
			model.addAttribute("firstName", user.getName());
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// get the user with access to this portfolio
		Portfolio portfolio = portfolioService.findById(portfolioId);
		
		model.addAttribute("users", portfolio.getUsers());
		
		return "portfolio_access";
	}
	
	// handle post for the access page
	@RequestMapping(value = "/access/{portfolioId}", method = RequestMethod.POST)
	public String addUserAccessToPortfolio(@PathVariable("portfolioId") long portfolioId, 
			@RequestParam(value="mail", required=false) String mail,
			Model model) {
		// check if the object exists
		if(!portfolioService.checkIfPortfolioExists(portfolioId)) {
			model.addAttribute("notFoundError", true);
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			User user = userService.findUserByEmail(userService.findLoggedInUsername());
			model.addAttribute("authorizationError", true);
			model.addAttribute("firstName", user.getName());
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// check if the e-mail address exists
		if(userService.countByEmail(mail) > 0) {
			portfolioService.addUserAccessToPortfolio(portfolioId, mail);
			model.addAttribute("success", mail + " added");
		}else {
			model.addAttribute("error", mail + " does not exist.");
		}
		
		// get the user with access to this portfolio
		Portfolio portfolio = portfolioService.findById(portfolioId);
		model.addAttribute("users", portfolio.getUsers());
		return "portfolio_access";
	}
	
	// handle revoking user access
	@RequestMapping(value = "/revokeUserAccess", method = RequestMethod.POST)
	public @ResponseBody String revokeUserAccessToPortfolio(@RequestParam(value="portfolioId", required=true) long portfolioId, 
			@RequestParam(value="userId", required=true) int userId,
			Model model) {
		// check if the object exists
		if(!portfolioService.checkIfPortfolioExists(portfolioId)) {
			return "<div class='alert alert-danger'>This portfolio does not exist</div>";
		}
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			return "<div class='alert alert-danger'>You are not authorized to this portfolio</div>";
		}
		
		return portfolioService.removeUserAccessToPortfolio(portfolioId, userId);
	}
	
	// handle get for the access page
	@RequestMapping(value = "/apiAccess/{portfolioId}", method = RequestMethod.GET)
	public String grantAccessApiToPortfolio(@PathVariable("portfolioId") long portfolioId, Model model) {
		// check if the object exists
		if(!portfolioService.checkIfPortfolioExists(portfolioId)) {
			model.addAttribute("notFoundError", true);
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			User user = userService.findUserByEmail(userService.findLoggedInUsername());
			model.addAttribute("authorizationError", true);
			model.addAttribute("firstName", user.getName());
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// get the api keys with access to this portfolio
		Portfolio portfolio = portfolioService.findById(portfolioId);
		model.addAttribute("apiKeysPortfolio", portfolio.getApiKeys());
		
		// get the api keys of the current user
		User user = userService.findUserByEmail(userService.findLoggedInUsername());
		List<ApiKey> apiKeysCurrentUser = apiKeyService.findByUser(user);
		model.addAttribute("apiKeysCurrentUser", apiKeysCurrentUser);
				
		return "portfolio_access_api";
	}
	
	@RequestMapping(value = "/apiAccess/{portfolioId}", method = RequestMethod.POST)
	public String handleNewApiAccess(@PathVariable("portfolioId") long portfolioId, 
			@RequestParam(value="apiKey", required=false) long apiKeyId,
			Model model) {
		// check if the object exists
		if(!portfolioService.checkIfPortfolioExists(portfolioId)) {
			model.addAttribute("notFoundError", true);
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		
		// get the current logged in user
		String username = userService.findLoggedInUsername();
		User currentUser = userService.findUserByEmail(username);
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			model.addAttribute("authorizationError", true);
			model.addAttribute("firstName", currentUser.getName());
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// get the API Key object
		ApiKey apiKey = apiKeyService.findById(apiKeyId);
		
		// check if the apiKey exists
		if(apiKey == null) {
			model.addAttribute("notFoundError", true);
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// check if the api key is registered by the current user
		if(!apiKey.getUser().equals(currentUser)) {
			model.addAttribute("firstName", currentUser.getName());
			model.addAttribute("object", "API Key");
			return "error_page";
		}

		// checks OK
		portfolioService.addApiAccessToPortfolio(apiKey, portfolioId);
		
		// now add changes to the model
		List<ApiKey> apiKeysCurrentUser = apiKeyService.findByUser(currentUser);
		model.addAttribute("apiKeysCurrentUser", apiKeysCurrentUser);
		Portfolio portfolio = portfolioService.findById(portfolioId);
		model.addAttribute("apiKeysPortfolio", portfolio.getApiKeys());
		
		model.addAttribute("success", "API Key successfully added");
		
		return "portfolio_access_api";
	}
	
	// handle revoking user access
	@RequestMapping(value = "/revokeApiAccess", method = RequestMethod.POST)
	public @ResponseBody String revokeApiAccessToPortfolio(@RequestParam(value="portfolioId", required=true) long portfolioId, 
			@RequestParam(value="apiKeyId", required=true) long apiKeyId,
			Model model) {
		// check if the object exists
		if(!portfolioService.checkIfPortfolioExists(portfolioId)) {
			return "<div class='alert alert-danger'>This portfolio does not exist</div>";
		}
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			return "<div class='alert alert-danger'>You are not authorized to this portfolio</div>";
		}
		
		return portfolioService.removeApiAccessToPortfolio(portfolioId, apiKeyId);
	}
	
	// handle revoking user access
	@RequestMapping(value = "/delete/{portfolioId}", method = RequestMethod.GET)
	public String confirmDelete(@PathVariable("portfolioId") long portfolioId, 
			Model model) {
		// check if the object exists
		if(!portfolioService.checkIfPortfolioExists(portfolioId)) {
			model.addAttribute("notFoundError", true);
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			User user = userService.findUserByEmail(userService.findLoggedInUsername());
			model.addAttribute("authorizationError", true);
			model.addAttribute("firstName", user.getName());
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// get the portfolio, and add it to the object
		Portfolio portfolio = portfolioService.findById(portfolioId);
		// add to model		
		model.addAttribute("portfolio", portfolio);
		model.addAttribute("portfolioId", portfolioId);
		
		// now get the attached wallets
		List<Wallet> wallets = walletService.getWalletsByPortfolio(portfolio);
		model.addAttribute("wallets", wallets);
		
		
		return "portfolio_confirm_delete";
	}
	
	// handle revoking user access
	@RequestMapping(value = "/delete/{portfolioId}", method = RequestMethod.POST)
	public String deletePortfolio(@PathVariable("portfolioId") long portfolioId, 
			Model model) {
		// check if the object exists
		if(!portfolioService.checkIfPortfolioExists(portfolioId)) {
			model.addAttribute("notFoundError", true);
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			User user = userService.findUserByEmail(userService.findLoggedInUsername());
			model.addAttribute("authorizationError", true);
			model.addAttribute("firstName", user.getName());
			model.addAttribute("object", "portfolio");
			return "error_page";
		}
		
		// get the portfolio, and add it to the object
		Portfolio portfolio = portfolioService.findById(portfolioId);
		
		portfolioService.deletePortfolio(portfolio);
		
		
		return "redirect:/portfolio/results";
	}
	
	@RequestMapping(value = "/chart/{portfolioId}", method = RequestMethod.GET)
    public String coinChart(
    		@PathVariable("portfolioId") long portfolioId,
    		@RequestParam(value="lastHours", required=false) Integer lastHours,
    		@RequestParam(value="intervalInMinutes", required=false) Integer intervalInMinutes,
    		Model model) {
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			User user = userService.findUserByEmail(userService.findLoggedInUsername());
			model.addAttribute("firstName", user.getName());
			model.addAttribute("object", "portfolio");
			return "not_authorized";
		}
		
		// check if the values are null
		if(lastHours == null) {
			lastHours = 1;
		}
		if(intervalInMinutes == null) {
			intervalInMinutes = 5;
		}
		
		// set the get parameters
		model.addAttribute("lastHours", lastHours);
		model.addAttribute("intervalInMinutes", intervalInMinutes);
		model.addAttribute("portfolioId", portfolioId);
		
		Portfolio portfolio = portfolioService.findById(portfolioId);
		model.addAttribute("portfolioName", portfolio.getName());
		
		
		FormOptions formOptions = new FormOptions();
		List<FormOption> hourOptions = formOptions.defaultSetHourOptions();
		// now add to the model
		model.addAttribute("hourOptions",hourOptions);
		
		// add the minute options
		List<FormOption> minuteOptions = formOptions.defaultSetMinuteOptions();
		// now add to the model
		model.addAttribute("minuteOptions",minuteOptions);
		
		return "portfolio_chart";
	}
	
	
	
	@RequestMapping(value = "/areachart", method = RequestMethod.GET)
    public String createAreaChartPortfolioValue(
    		@RequestParam(value="portfolioId", required=true) Long portfolioId,
    		@RequestParam(value="lastHours", required=true) Integer lastHours,
    		@RequestParam(value="intervalInMinutes", required=true) Integer intervalInMinutes,
    		Model model) {
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			User user = userService.findUserByEmail(userService.findLoggedInUsername());
			model.addAttribute("firstName", user.getName());
			model.addAttribute("object", "portfolio");
			return "not_authorized";
		}
		
		// get the wallets
		List<Wallet> wallets = walletService.findByPortfolio_Id(portfolioId);
		
		// loop the wallets to update the current coin value
		List<String> walletAddresses = new ArrayList<>();
		for(Wallet wallet : wallets) {
			// add the wallet name to the list
			walletAddresses.add(wallet.getCensoredWalletAddress() + " (" + wallet.getCoin().getCoinMarketCapCoin().getSymbol() + ")");
		}
		
		// now determine the begin and end time
		Calendar start = Calendar.getInstance();
		start.add(Calendar.HOUR_OF_DAY, -lastHours);
		start.set(Calendar.SECOND, 0);
		
		Calendar end = Calendar.getInstance();
		end.set(Calendar.SECOND, 0);
		
		List<PortfolioChartLine> portfolioChartLines = new ArrayList<>();
		
		// loop through the times
		for (Date date = start.getTime(); start.before(end) || start.equals(end); start.add(Calendar.MINUTE, intervalInMinutes), date = start.getTime()) {
			Calendar startInterval = Calendar.getInstance();
			startInterval.setTime(date);
			startInterval.set(Calendar.SECOND, 0);
			
			Calendar endInterval = Calendar.getInstance();
			endInterval.setTime(date);
			endInterval.add(Calendar.MINUTE, intervalInMinutes);
			endInterval.add(Calendar.SECOND, -1);
			
			PortfolioChartLine portfolioChartLine = new PortfolioChartLine();
			portfolioChartLine.setDate(endInterval.getTime());
			
			double totalInvestment = 0;
			
			// loop through the wallets again
			for(Wallet wallet : wallets) {
				Wallet historicalWallet = new Wallet(wallet);
				historicalWallet = walletService.getWalletHistoricalValues(historicalWallet, startInterval.getTime(), endInterval.getTime());
				
				// add this investment to the total investment
				totalInvestment += historicalWallet.getCurrentWalletInvestment();
				
				double value = historicalWallet.getCurrentWalletValue();
				
				PortfolioChartLineWallet portfolioChartLineWallet = new PortfolioChartLineWallet();
				portfolioChartLineWallet.setWalletName(historicalWallet.getCensoredWalletAddress() + " (" + wallet.getCoin().getCoinMarketCapCoin().getSymbol() + ")");
				portfolioChartLineWallet.setWalletValue(value);
				
				// now push it to the portfolioChartLine
				portfolioChartLine.getPortfolioChartLineWallets().add(portfolioChartLineWallet);
				
			}
			
			// add the total investment
			portfolioChartLine.setTotalInvested(totalInvestment);
			
			// push it to the list
			portfolioChartLines.add(portfolioChartLine);
			
		}
		
		model.addAttribute("portfolioChartLines",portfolioChartLines);
		model.addAttribute("walletAddresses",walletAddresses);
		
		return "portfolio_areachart";
	}
	

	@RequestMapping(value = "/piechart", method = RequestMethod.GET)
    public String createPieChartPortfolioDistribution(
    		@RequestParam(value="portfolioId", required=true) Long portfolioId,
    		Model model) {
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			User user = userService.findUserByEmail(userService.findLoggedInUsername());
			model.addAttribute("firstName", user.getName());
			model.addAttribute("object", "portfolio");
			return "not_authorized";
		}
		
		// get the portfolio
		Portfolio portfolio = portfolioService.findById(portfolioId);
		
		// get the wallets
		List<Wallet> wallets = walletService.findByPortfolio_Id(portfolioId);
		
		
		List<PortfolioPieChartValue> portfolioPieChartValues = new ArrayList<>();
		
		for(Wallet wallet : wallets) {
			
			// now add to the PortfolioPieChartValue object
			PortfolioPieChartValue portfolioPieChartValue = new PortfolioPieChartValue();
			portfolioPieChartValue.setWalletAddress(wallet.getCensoredWalletAddress() + " (" + wallet.getCoin().getCoinMarketCapCoin().getSymbol() + ")");
			portfolioPieChartValue.setCurrentWalletValue(wallet.getCurrentWalletValue());
			
			// add to the list
			portfolioPieChartValues.add(portfolioPieChartValue);
		}
		
		model.addAttribute("portfolioPieChartValues",portfolioPieChartValues);
		model.addAttribute("portfolioName",portfolio.getName());
		
		return "portfolio_piechart";
	}
	
	@RequestMapping(value = "/linechart", method = RequestMethod.GET)
    public String createLineChartPortfolioROI(
    		@RequestParam(value="portfolioId", required=true) Long portfolioId,
    		@RequestParam(value="lastHours", required=true) Integer lastHours,
    		@RequestParam(value="intervalInMinutes", required=true) Integer intervalInMinutes,
    		Model model) {
		
		// check if the current user has access to this portfolio
		boolean access = userService.checkIfCurrentUserIsAuthorizedToPortfolio(portfolioId);
		if(!access) {
			User user = userService.findUserByEmail(userService.findLoggedInUsername());
			model.addAttribute("firstName", user.getName());
			model.addAttribute("object", "portfolio");
			return "not_authorized";
		}
		
		// get the wallets
		List<Wallet> wallets = walletService.findByPortfolio_Id(portfolioId);
		
		// get the portfolio
		Portfolio portfolio = portfolioService.findById(portfolioId);
		
		// now determine the begin and end time
		Calendar start = Calendar.getInstance();
		start.add(Calendar.HOUR_OF_DAY, -lastHours);
		start.set(Calendar.SECOND, 0);
		
		Calendar end = Calendar.getInstance();
		end.set(Calendar.SECOND, 0);
		
		List<PortfolioLineChartRoiValue> portfolioLineChartRoiValues = new ArrayList<>();
		
		// loop through the times
		for (Date date = start.getTime(); start.before(end) || start.equals(end); start.add(Calendar.MINUTE, intervalInMinutes), date = start.getTime()) {
			Calendar startInterval = Calendar.getInstance();
			startInterval.setTime(date);
			startInterval.set(Calendar.SECOND, 0);
			
			Calendar endInterval = Calendar.getInstance();
			endInterval.setTime(date);
			endInterval.add(Calendar.MINUTE, intervalInMinutes);
			endInterval.add(Calendar.SECOND, -1);
			
			PortfolioLineChartRoiValue portfolioLineChartRoiValue = new PortfolioLineChartRoiValue();
			
			
			// portfolio values
			double totalValue = 0;			// the total value of the portfolio
			double totalInvestment = 0; 		// the total amount of investment
			double totalDeposited = 0;
			
			// loop through the wallets again
			for(Wallet wallet : wallets) {
				Wallet historicalWallet = new Wallet(wallet);
				historicalWallet = walletService.getWalletHistoricalValues(historicalWallet, startInterval.getTime(), endInterval.getTime());
				
				
				double value = historicalWallet.getCurrentWalletValue();
				// add to the totalValue
				totalValue += value;
				
				// calculate the investment for this wallet
				totalInvestment += historicalWallet.getCurrentWalletInvestment();
				totalDeposited += historicalWallet.getCurrentWalletDeposited();
			}
			
			// add to the portfolioLineChartRoiValue
			portfolioLineChartRoiValue.setDate(endInterval.getTime());
			
			// calculate the roi
			double profitLoss = totalValue - totalInvestment;
			double roi = profitLoss / totalDeposited;
			
			portfolioLineChartRoiValue.setRoi(roi);
			
			// add to the list
			portfolioLineChartRoiValues.add(portfolioLineChartRoiValue);
		}
		
		model.addAttribute("portfolioLineChartRoiValues",portfolioLineChartRoiValues);
		model.addAttribute("portfolioName",portfolio.getName());
		
		return "portfolio_linechart";
	}
	
	
}
