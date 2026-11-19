package ir.expense.tracker.makharej.common;


import ir.expense.tracker.makharej.common.exception.DomainException;
import ir.expense.tracker.makharej.common.exception.InvalidToken;
import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		Map<String , String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {

			String fieldName = ((FieldError) error).getField();
			String message = error.getDefaultMessage();
			errors.put(fieldName, message);
		});
		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Since Spring 6 the framework exceptions handled by {@link ResponseEntityExceptionHandler} (malformed JSON,
	 * missing parameter, wrong method/media type, ...) are rendered as an RFC 7807 {@link ProblemDetail} body.
	 * This API has always answered those with the bare status code and no body, so keep doing that.
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

		ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
		if (response != null && response.getBody() instanceof ProblemDetail) {
			return new ResponseEntity<>(null, response.getHeaders(), response.getStatusCode());
		}
		return response;
	}

	/**
	 * Spring 6.1+ throws {@link NoResourceFoundException} for unmapped paths, where the resource handler used to
	 * call {@code response.sendError(404)} (which Spring Boot renders through its /error page). Do the same.
	 */
	@Override
	protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		if (request instanceof ServletWebRequest servletWebRequest && servletWebRequest.getResponse() != null) {
			try {
				servletWebRequest.getResponse().sendError(HttpStatus.NOT_FOUND.value());
			}
			catch (IOException ioException) {
				log.warn("could not send 404 for {}", request, ioException);
			}
		}
		return null;
	}

	@org.springframework.web.bind.annotation.ExceptionHandler(value = InvalidToken.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<?> handleException(InvalidToken ex) {

		ErrorResponse errorResponse = ErrorResponse.builder().statusCode(HttpStatus.UNAUTHORIZED).message(ex.getMessage()).build();
		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	@org.springframework.web.bind.annotation.ExceptionHandler(value = DomainException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<?> handleException(DomainException ex) {

		ErrorResponse errorResponse = ErrorResponse.builder().statusCode(HttpStatus.BAD_REQUEST).message(ex.getMessage()).build();
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@org.springframework.web.bind.annotation.ExceptionHandler(value = Exception.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public @ResponseBody ResponseEntity<?> handleException(Exception ex) {

		ErrorResponse errorResponse = ErrorResponse.builder().statusCode(HttpStatus.INTERNAL_SERVER_ERROR).message(ErrorMessages.INTERNAL_ERROR).build();
		log.error("internal error occurred!", ex);
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
