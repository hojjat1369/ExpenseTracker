package ir.expense.tracker.makharej.dto.report;


import com.fasterxml.jackson.annotation.JsonFormat;
import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * Which expenses go into the report: the same filters as the expense search.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {

	@NotNull(message = ErrorMessages.PERIOD_CANNOT_BE_NULL)
	private ReportPeriod period;

	private String name;
	private String tag;
	@JsonFormat(pattern= "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tehran")
	private Date fromDate;
	@JsonFormat(pattern= "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tehran")
	private Date toDate;

	private Long categoryId;
}
