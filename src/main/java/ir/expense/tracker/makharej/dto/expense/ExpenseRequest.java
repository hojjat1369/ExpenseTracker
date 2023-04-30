package ir.expense.tracker.makharej.dto.expense;


import com.fasterxml.jackson.annotation.JsonFormat;
import ir.expense.tracker.makharej.common.messages.ErrorMessages;
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
public class ExpenseRequest {

	@NotBlank(message = ErrorMessages.NAME_CANNOT_BE_NULL_OR_EMPTY)
	@NotNull(message = ErrorMessages.NAME_CANNOT_BE_NULL_OR_EMPTY)
	private String name;

	@NotNull(message = ErrorMessages.AMOUNT_CANNOT_BE_NULL)
	private Double amount;
	private String tag;
	private String note;

	@JsonFormat(pattern = "yyyy-mm-dd HH:mm")
	@NotNull(message = ErrorMessages.DATE_CANNOT_BE_NULL)
	private Date expenseDate;

	@NotNull(message = ErrorMessages.CATEGORY_CANNOT_BE_NULL)
	private Long categoryId;
}
