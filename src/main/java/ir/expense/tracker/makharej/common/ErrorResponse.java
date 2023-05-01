package ir.expense.tracker.makharej.common;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

	private HttpStatus statusCode;
	private String message;

	public ErrorResponse(String message) {

		super();
		this.message = message;
	}
}
