package ir.expense.tracker.makharej.dto.report;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {

	private ReportPeriod period;
	private Double totalAmount;
	private Long count;
	private List<ReportPeriodResponse> periods;
}
