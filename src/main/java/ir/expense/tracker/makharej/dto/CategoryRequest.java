package ir.expense.tracker.makharej.dto;


import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class CategoryRequest {

	private String name;
}
