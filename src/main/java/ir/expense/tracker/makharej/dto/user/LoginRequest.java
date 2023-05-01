package ir.expense.tracker.makharej.dto.user;


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
public class LoginRequest {

	@NotNull(message = ErrorMessages.USERNAME_CANNOT_BE_NULL)
	@NotEmpty(message = ErrorMessages.USERNAME_CANNOT_BE_NULL)
	private String username;
	@NotNull(message = ErrorMessages.PASSWORD_CANNOT_BE_NULL)
	@NotEmpty(message = ErrorMessages.PASSWORD_CANNOT_BE_NULL)
	private String password;
}
