package io.core.experts.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.core.experts.manager.repositories.CoinRepository;
import io.core.experts.manager.repositories.CoinValueRepository;
import io.core.experts.manager.services.CoinService;
import io.core.experts.manager.services.CoinValueService;

@Component
@ComponentScan(basePackages = { "nl.kolkos.cryptoManager.*" })
public class ScheduledTasks {
	@Autowired
	private CoinService coinService;
		
	@Scheduled(fixedRate = 300_000)
    public void reportCurrentTime() {
        // use the service
		coinService.updateCoinValues();
	
    }
	
	
}
