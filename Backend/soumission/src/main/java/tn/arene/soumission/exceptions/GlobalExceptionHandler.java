package tn.arene.soumission.exceptions;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import tn.arene.soumission.dto.ErrorResponse;

/** Traduit les exceptions applicatives en enveloppe {@link ErrorResponse}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException exception, WebRequest request) {
		return build(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException exception, WebRequest request) {
		return build(HttpStatus.CONFLICT, exception.getMessage(), request);
	}

	@ExceptionHandler({ IllegalArgumentException.class, HttpMessageNotReadableException.class })
	public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception, WebRequest request) {
		return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, WebRequest request) {
		String path = request.getDescription(false).replaceFirst("^uri=", "");
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				path);
		return ResponseEntity.status(status).body(body);
	}
}
