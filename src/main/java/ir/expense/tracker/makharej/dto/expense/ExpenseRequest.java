package ir.expense.tracker.makharej.dto.expense;


import com.fasterxml.jackson.annotation.JsonFormat;
import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import ir.expense.tracker.makharej.dto.user.Userable;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Locale;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ExpenseRequest extends Userable {

	@NotBlank(message = ErrorMessages.NAME_CANNOT_BE_NULL_OR_EMPTY)
	@NotNull(message = ErrorMessages.NAME_CANNOT_BE_NULL_OR_EMPTY)
	private String name;

	@NotNull(message = ErrorMessages.AMOUNT_CANNOT_BE_NULL)
	private Double amount;
	private String tag;
	private String note;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern= "yyyy-MM-dd HH:mm:ss", locale = "Asia/Tehran")
	@NotNull(message = ErrorMessages.DATE_CANNOT_BE_NULL)
	private Date expenseDate;

	@NotNull(message = ErrorMessages.CATEGORY_CANNOT_BE_NULL)
	private Long categoryId;
}
