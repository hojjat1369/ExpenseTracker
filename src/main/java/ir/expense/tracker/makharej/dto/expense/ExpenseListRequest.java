package ir.expense.tracker.makharej.dto.expense;


import com.fasterxml.jackson.annotation.JsonFormat;
import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import ir.expense.tracker.makharej.dto.user.Userable;
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
public class ExpenseListRequest extends Userable {

	private String name;
	private String tag;
	@JsonFormat(pattern= "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tehran")
	private Date fromDate;
	@JsonFormat(pattern= "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tehran")
	private Date toDate;

	private Long categoryId;
}
