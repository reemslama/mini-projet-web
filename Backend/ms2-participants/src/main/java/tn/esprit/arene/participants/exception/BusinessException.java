package tn.esprit.arene.participants.exception;

/** Règle métier violée (doublon, équipe complète...) -> HTTP 409 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
