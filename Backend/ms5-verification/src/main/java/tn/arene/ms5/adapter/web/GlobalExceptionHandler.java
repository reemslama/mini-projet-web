package tn.arene.ms5.adapter.web;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import tn.arene.ms5.adapter.web.dto.ErrorResponse;
import tn.arene.ms5.application.AiUnavailableException;
import tn.arene.ms5.application.NotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> notFound(NotFoundException exception, WebRequest request) {
		return error(HttpStatus.NOT_FOUND, exception, request);
	}

	@ExceptionHandler(AiUnavailableException.class)
	public ResponseEntity<ErrorResponse> aiUnavailable(AiUnavailableException exception, WebRequest request) {
		return error(HttpStatus.SERVICE_UNAVAILABLE, exception, request);
	}

	@ExceptionHandler({ IllegalArgumentException.class, HttpMessageNotReadableException.class,
			MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ErrorResponse> badRequest(RuntimeException exception, WebRequest request) {
		return error(HttpStatus.BAD_REQUEST, exception, request);
	}

	private ResponseEntity<ErrorResponse> error(HttpStatus status, RuntimeException exception, WebRequest request) {
		String path = request.getDescription(false).replaceFirst("^uri=", "");
		ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
				exception.getMessage(), path);
		return ResponseEntity.status(status).body(body);
	}
}
