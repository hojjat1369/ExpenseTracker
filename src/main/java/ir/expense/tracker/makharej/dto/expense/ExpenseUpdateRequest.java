package ir.expense.tracker.makharej.dto.expense;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ir.expense.tracker.makharej.common.util.TehranDateTimeDeserializer;
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
public class ExpenseUpdateRequest {

	private Long id;
	private String name;
	private Double amount;
	private String tag;
	private String note;
	@JsonDeserialize(using = TehranDateTimeDeserializer.class)
	private Date expenseDate;
	private Long categoryId;
}
