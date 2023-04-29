package ir.expense.tracker.makharej.dto.category;


import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

	@NotNull(message = ErrorMessages.NAME_CANNOT_BE_NULL_OR_EMPTY)
	@NotEmpty(message = ErrorMessages.NAME_CANNOT_BE_NULL_OR_EMPTY)
	private String name;
}
