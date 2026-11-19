package ir.expense.tracker.makharej;


import ir.expense.tracker.makharej.service.ReportService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;


@Configuration
public class ClockConfig {

	@Bean
	public Clock clock() {

		return Clock.system(ReportService.ZONE);
	}
}
