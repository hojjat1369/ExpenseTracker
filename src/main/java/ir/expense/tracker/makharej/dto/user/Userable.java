package ir.expense.tracker.makharej.dto.user;


import io.swagger.annotations.ApiModelProperty;
import lombok.*;


@Getter
@Setter
public class Userable {

	@ApiModelProperty(required = false, hidden = true)
	private Long userId;
}
