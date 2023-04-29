package ir.expense.tracker.makharej.dto.category;


import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryListRequest {

	private String name;
}
