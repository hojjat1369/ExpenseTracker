package ir.expense.tracker.makharej.dto.trigger;


import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import ir.expense.tracker.makharej.dto.report.ReportPeriod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriggerRequest {

	@NotNull(message = ErrorMessages.NAME_CANNOT_BE_NULL_OR_EMPTY)
	@NotEmpty(message = ErrorMessages.NAME_CANNOT_BE_NULL_OR_EMPTY)
	private String name;

	@NotNull(message = ErrorMessages.PERIOD_CANNOT_BE_NULL)
	private ReportPeriod period;

	@NotNull(message = ErrorMessages.LIMIT_CANNOT_BE_NULL)
	@Positive(message = ErrorMessages.LIMIT_MUST_BE_POSITIVE)
	private Double limit;

	private String expenseName;
	private String tag;
	private Long categoryId;
}
