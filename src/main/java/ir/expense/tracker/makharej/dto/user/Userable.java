package ir.expense.tracker.makharej.dto.user;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


@Getter
@Setter
public class Userable {

	@Schema(hidden = true)
	private Long userId;
}
