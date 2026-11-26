package tn.arene.soumission.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Violation d'une règle métier (deadline, commit figé, transition interdite) -> HTTP 409. */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}
}
