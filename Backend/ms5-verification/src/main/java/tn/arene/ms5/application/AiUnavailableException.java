package tn.arene.ms5.application;

/** Service IA absent : 503, sans effet sur le verdict ni la similarité. */
public class AiUnavailableException extends RuntimeException {
	public AiUnavailableException(String message) {
		super(message);
	}
}
