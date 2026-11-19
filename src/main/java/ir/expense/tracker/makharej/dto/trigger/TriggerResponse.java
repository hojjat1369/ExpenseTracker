package ir.expense.tracker.makharej.dto.trigger;


import ir.expense.tracker.makharej.dto.report.ReportPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriggerResponse {

	private Long id;
	private String name;
	private ReportPeriod period;
	private Double limit;
	private String expenseName;
	private String tag;
	private Long categoryId;

	/** The month ("2023-05") or year ("2023") that the amount below is about: the current one. */
	private String currentPeriod;
	/** What the matching expenses add up to in the current period. */
	private Double currentAmount;
	/** true when currentAmount is more than the limit. */
	private Boolean triggered;
}
