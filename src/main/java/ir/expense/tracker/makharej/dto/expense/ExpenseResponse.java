package ir.expense.tracker.makharej.dto.expense;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponse {

	private Long id;
	private String name;
	private Double amount;
	private String tag;
	private String note;
	private Date expenseDate;
	private Long categoryId;
}
