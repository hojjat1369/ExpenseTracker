package ir.expense.tracker.makharej.dto;


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

	@NotNull
	@NotEmpty
	private String name;
}
