package ir.expense.tracker.makharej.dto.expense;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseUpdateRequest {

	private Long id;
	private String name;
	private Double amount;
	private String tag;
	private String note;
	@JsonFormat(pattern = "yyyy-mm-dd HH:mm")
	private Date expenseDate;
}
