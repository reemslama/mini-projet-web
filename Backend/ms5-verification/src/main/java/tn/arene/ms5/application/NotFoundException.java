package tn.arene.ms5.application;

/** Ressource MS5 introuvable (404 convention). */
public class NotFoundException extends RuntimeException {
	public NotFoundException(String message) {
		super(message);
	}
}
