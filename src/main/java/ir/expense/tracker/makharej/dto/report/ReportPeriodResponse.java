package ir.expense.tracker.makharej.dto.report;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportPeriodResponse {

	/** "2023-05" for a month, "2023" for a year. */
	private String period;
	private Double totalAmount;
	private Long count;
}
