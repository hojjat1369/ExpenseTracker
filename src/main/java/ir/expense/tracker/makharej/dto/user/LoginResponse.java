package ir.expense.tracker.makharej.dto.user;


import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

	private String token;
	private String username;
}
