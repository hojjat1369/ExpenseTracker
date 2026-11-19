package ir.expense.tracker.makharej.dto.category;


import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import ir.expense.tracker.makharej.dto.user.Userable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest extends Userable {

	@NotNull(message = ErrorMessages.NAME_CANNOT_BE_NULL_OR_EMPTY)
	@NotEmpty(message = ErrorMessages.NAME_CANNOT_BE_NULL_OR_EMPTY)
	private String name;
}
